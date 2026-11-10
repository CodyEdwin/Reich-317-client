package com.elvarg.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

/**
 * Presents the client's {@link AndroidDisplay} framebuffer and turns touches and
 * key presses into the events the client's input handlers expect. The soft
 * keyboard is routed through an {@link InputConnection} so typed characters
 * reach the client's character queue (login fields, chat, etc.).
 */
public final class GameView extends View {

    private ClientEngine engine;
    private final RectF dest = new RectF();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int pointer = -1;

    /** Two-finger vertical drag scrolls the wheel (chat, interfaces, zoom). */
    private boolean scrolling;
    private float scrollAnchorY;

    /** When set, the next press is delivered as a right click. */
    public volatile boolean rightClickNext;

    /** Notified when the user asks for the overlay menu (right click / long press). */
    public interface OverlayMenuListener {
        void onOverlayMenu(int canvasX, int canvasY);
    }

    private OverlayMenuListener overlayMenuListener;

    /** Long press opens the overlay menu on touch devices. */
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int longPressX;

    private int longPressY;

    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            notifyOverlayMenu(longPressX, longPressY);
        }
    };

    public void setOverlayMenuListener(OverlayMenuListener listener) {
        this.overlayMenuListener = listener;
    }

    /** A copy of the current frame, for the screenshot feature. */
    public Bitmap snapshot() {
        synchronized (AndroidDisplay.LOCK) {
            Bitmap front = AndroidDisplay.frontBuffer();
            if (front == null) {
                return null;
            }
            return front.copy(front.getConfig(), false);
        }
    }

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(0xFF000000);
        setFocusable(true);
        setFocusableInTouchMode(true);
        // Hover events (a mouse moving over the view without a click). Setting
        // a hover listener also makes the view hoverable, which is what the
        // framework requires before it dispatches ACTION_HOVER_* events.
        setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                handleMouse(event, event.getActionMasked());
                return true;
            }
        });
    }

    public void setEngine(ClientEngine engine) {
        this.engine = engine;
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ClientEngine engine = this.engine;
        if (engine == null) {
            paint.setColor(0xFFFFFFFF);
            paint.setTextSize(42f * getResources().getDisplayMetrics().density);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Preparing cache\u2026", getWidth() / 2f, getHeight() / 2f, paint);
            return;
        }

        float scale;
        if (ViewScaling.isKeepAspectRatio()) {
            scale = Math.min(getWidth() / (float) AndroidDisplay.width,
                    getHeight() / (float) AndroidDisplay.height);
        } else {
            // Stretched: fill the window, ignoring the aspect ratio.
            scale = Math.max(getWidth() / (float) AndroidDisplay.width,
                    getHeight() / (float) AndroidDisplay.height);
        }
        scale *= ViewScaling.getScalingFactor() / 100f;
        if (ViewScaling.isIntegerScaling() && scale >= 1f) {
            scale = (float) Math.floor(scale);
        }
        float dw = AndroidDisplay.width * scale;
        float dh = AndroidDisplay.height * scale;
        float left = (getWidth() - dw) / 2f;
        float top = (getHeight() - dh) / 2f;
        dest.set(left, top, left + dw, top + dh);
        paint.setFilterBitmap(!ViewScaling.isIncreasedPerformance());

        // The game thread composes the frame (game + overlays) in a bitmap and
        // swaps it in; the view only has to draw it.
        synchronized (AndroidDisplay.LOCK) {
            Bitmap frame = AndroidDisplay.frontBuffer();
            if (frame != null) {
                canvas.drawBitmap(frame, null, dest, paint);
            }
        }

        postInvalidateOnAnimation();
    }

    private int toCanvasX(float screenX) {
        float sx = dest.width() / AndroidDisplay.width;
        if (sx <= 0f) sx = 1f;
        int x = Math.round((screenX - dest.left) / sx);
        return Math.max(0, Math.min(AndroidDisplay.width - 1, x));
    }

    private int toCanvasY(float screenY) {
        float sy = dest.height() / AndroidDisplay.height;
        if (sy <= 0f) sy = 1f;
        int y = Math.round((screenY - dest.top) / sy);
        return Math.max(0, Math.min(AndroidDisplay.height - 1, y));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        ClientEngine engine = this.engine;
        if (engine == null) {
            return false;
        }

        int action = event.getActionMasked();

        // A paired Bluetooth mouse reports its own buttons; a finger does not.
        if (isMouse(event)) {
            return handleMouse(event, action);
        }

        // Any device can scroll (mouse wheel, stylus, trackpad).
        if (action == MotionEvent.ACTION_SCROLL) {
            scroll(event);
            return true;
        }

        int index = event.getActionIndex();
        int id = event.getPointerId(index);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                scrolling = false;
                pointer = id;
                engine.mouseMove(toCanvasX(event.getX(index)), toCanvasY(event.getY(index)));
                boolean right = rightClickNext;
                rightClickNext = false;
                engine.mouseDown(toCanvasX(event.getX(index)), toCanvasY(event.getY(index)), right);
                scheduleLongPress(toCanvasX(event.getX(index)), toCanvasY(event.getY(index)));
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2) {
                    // A second finger turns the gesture into a wheel scroll.
                    // Cancel any click started by the first finger first.
                    scrolling = true;
                    if (pointer != -1) {
                        engine.mouseUp();
                        pointer = -1;
                    }
                    engine.mouseMove(toCanvasX(averageX(event)), toCanvasY(averageY(event)));
                    scrollAnchorY = averageY(event);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (scrolling && event.getPointerCount() >= 2) {
                    engine.mouseMove(toCanvasX(averageX(event)), toCanvasY(averageY(event)));
                    float delta = averageY(event) - scrollAnchorY;
                    float threshold = 24f * getResources().getDisplayMetrics().density;
                    if (Math.abs(delta) >= threshold) {
                        int notches = (int) (delta / threshold);
                        // Natural touch direction: dragging up scrolls down.
                        engine.mouseWheel(delta < 0 ? 1 : -1);
                        scrollAnchorY += notches * threshold;
                    }
                    return true;
                }
                if (pointer != -1) {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        if (event.getPointerId(i) == pointer) {
                            int x = toCanvasX(event.getX(i));
                            int y = toCanvasY(event.getY(i));
                            cancelPendingLongPress();
                            engine.mouseDrag(x, y);
                        }
                    }
                }
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (scrolling) {
                    scrolling = false;
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                scrolling = false;
                cancelPendingLongPress();
                if (id == pointer) {
                    pointer = -1;
                    engine.mouseUp();
                }
                return true;
            default:
                return true;
        }
    }

    private static float averageX(MotionEvent event) {
        float sum = 0f;
        for (int i = 0; i < event.getPointerCount(); i++) {
            sum += event.getX(i);
        }
        return sum / event.getPointerCount();
    }

    private static float averageY(MotionEvent event) {
        float sum = 0f;
        for (int i = 0; i < event.getPointerCount(); i++) {
            sum += event.getY(i);
        }
        return sum / event.getPointerCount();
    }

    /** Forwards a mouse wheel scroll (finger drag on the right side of the screen). */
    public void scroll(int rotation) {
        ClientEngine engine = this.engine;
        if (engine != null) {
            engine.mouseWheel(rotation);
        }
    }

    // ---- Bluetooth mouse / trackpad ---------------------------------------

    /**
     * Whether the event came from a pointer device with buttons (a paired
     * Bluetooth mouse or a trackpad) rather than from a finger.
     */
    private static boolean isMouse(MotionEvent event) {
        if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            return true;
        }
        for (int i = 0; i < event.getPointerCount(); i++) {
            if (event.getToolType(i) == MotionEvent.TOOL_TYPE_MOUSE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Maps a mouse event onto the client's mouse handler.
     *
     * <p>The secondary button becomes a right click, so a Bluetooth mouse
     * behaves like the desktop one and the on-screen right-click toggle is not
     * needed. Hovering moves the cursor without clicking, and the wheel is
     * forwarded as a wheel rotation.
     */
    private boolean handleMouse(MotionEvent event, int action) {
        ClientEngine engine = this.engine;
        if (engine == null) {
            return false;
        }

        int x = toCanvasX(event.getX());
        int y = toCanvasY(event.getY());

        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                pointer = event.getPointerId(0);
                engine.mouseMove(x, y);
                int buttons = event.getButtonState();
                boolean right = (buttons & MotionEvent.BUTTON_SECONDARY) != 0;
                engine.mouseDown(x, y, right);
                if (right) {
                    // A right click opens the menu of the overlay under the
                    // cursor (the desktop client does the same).
                    notifyOverlayMenu(x, y);
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE:
                if (pointer != -1) {
                    engine.mouseDrag(x, y);
                } else {
                    engine.mouseMove(x, y);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pointer != -1) {
                    pointer = -1;
                    engine.mouseUp();
                }
                return true;
            case MotionEvent.ACTION_HOVER_MOVE:
            case MotionEvent.ACTION_HOVER_ENTER:
                engine.mouseMove(x, y);
                return true;
            case MotionEvent.ACTION_SCROLL:
                scroll(event);
                return true;
            default:
                return true;
        }
    }

    /** Turns a scroll gesture into wheel rotations. */
    private void scroll(MotionEvent event) {
        float vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
        if (vscroll != 0f) {
            scroll((int) vscroll);
        }
    }

    private void notifyOverlayMenu(int canvasX, int canvasY) {
        OverlayMenuListener listener = overlayMenuListener;
        if (listener != null) {
            listener.onOverlayMenu(canvasX, canvasY);
        }
    }

    private void scheduleLongPress(int canvasX, int canvasY) {
        longPressX = canvasX;
        longPressY = canvasY;
        handler.postDelayed(longPressRunnable, 500L);
    }

    private void cancelPendingLongPress() {
        handler.removeCallbacks(longPressRunnable);
    }

    // ---- Soft keyboard ----------------------------------------------------

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_NONE
                | EditorInfo.IME_FLAG_NO_FULLSCREEN
                | EditorInfo.IME_FLAG_NO_EXTRACT_UI;
        return new BaseInputConnection(this, false) {
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                ClientEngine e = engine;
                if (e != null && text != null) {
                    for (int i = 0; i < text.length(); i++) {
                        e.keyDown(0, text.charAt(i));
                    }
                }
                return true;
            }

            @Override
            public boolean setComposingText(CharSequence text, int newCursorPosition) {
                return true;
            }

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                ClientEngine e = engine;
                if (e != null) {
                    for (int i = 0; i < beforeLength; i++) {
                        e.keyDown(KeyMap.VK_BACK_SPACE, (char) 8);
                    }
                }
                return true;
            }

            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                ClientEngine e = engine;
                if (e != null) {
                    int code = KeyMap.toAwtKeyCode(event.getKeyCode());
                    char ch = (char) event.getUnicodeChar();
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        e.keyDown(code, ch);
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        e.keyUp(code, ch);
                    }
                }
                return true;
            }
        };
    }
}
