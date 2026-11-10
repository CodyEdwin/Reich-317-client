package com.runescape.engine;

import com.elvarg.android.AndroidDisplay;
import com.runescape.Client;
import com.runescape.UserPreferences;
import com.runescape.draw.ProducingGraphicsBuffer;
import com.runescape.engine.impl.MouseHandler;
import com.runescape.engine.task.Clock;
import com.runescape.engine.task.TaskHandler;
import com.runescape.engine.task.TaskUtils;
import com.runescape.engine.task.impl.MilliClock;
import com.runescape.engine.task.impl.NanoClock;
import com.runescape.util.Bounds;
import net.runelite.api.events.CanvasSizeChanged;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.hooks.DrawCallbacks;
import net.runelite.rs.api.RSGameEngine;

/**
 * Android replacement for the desktop {@code GameEngine}.
 *
 * <p>The desktop engine extends {@code java.applet.Applet} and drives the game
 * loop, feeding AWT events into the client and blitting the frame onto an AWT
 * {@code Canvas}. Android has no AWT, so this version keeps the identical game
 * loop and the same engine state the client reads ({@code canvasWidth},
 * {@code canvasHeight}, {@code fps}, {@link #method2692()}, {@link #canvas},
 * ...), but renders into {@link AndroidDisplay} and receives input through
 * explicit methods the app calls from its touch/key handlers.
 *
 * <p>Field and method names match the desktop class so {@code Client} is
 * unchanged apart from the AWT-specific spots documented in {@code PORTING.md}.
 */
public abstract class GameEngine implements Runnable, RSGameEngine {

    public static TaskHandler taskHandler;
    static GameEngine gameEngine;
    public static int canvasWidth;
    public static int canvasHeight;
    static int threadCount;
    static long stopTimeMs;
    static boolean isKilled;
    static int cycleDurationMillis;

    private Thread thread;

    @Override
    public Thread getClientThread() {
        return thread;
    }

    @Override
    public boolean isClientThread() {
        return thread == Thread.currentThread();
    }

    static int fiveOrOne;
    protected static int fps;
    static long[] graphicsTickTimes;
    static long[] clientTickTimes;
    static int field209;
    static int field199;
    static volatile boolean volatileFocus;
    static long garbageCollectorTime;
    static long lastGarbageCollect;

    boolean hasErrored;
    protected int contentWidth;
    protected int contentHeight;
    int canvasX;
    int canvasY;
    int maxCanvasWidth;
    int maxCanvasHeight;

    public Canvas canvas;

    volatile boolean fullRedraw;
    boolean resizeCanvasNextFrame;
    volatile boolean isCanvasInvalid;
    volatile long field185;

    private static final long serialVersionUID = 1L;
    private static int viewportColor;

    static {
        gameEngine = null;
        threadCount = 0;
        stopTimeMs = 0L;
        isKilled = false;
        cycleDurationMillis = 20;
        fiveOrOne = 1;
        fps = 0;
        graphicsTickTimes = new long[32];
        clientTickTimes = new long[32];
        field199 = 500;
        volatileFocus = true;
        garbageCollectorTime = -1L;
        lastGarbageCollect = -1L;
    }

    protected GameEngine() {
        hasErrored = false;
        canvasX = 0;
        canvasY = 0;
        fullRedraw = true;
        resizeCanvasNextFrame = false;
        isCanvasInvalid = false;
        field185 = 0L;
        contentWidth = AndroidDisplay.width;
        contentHeight = AndroidDisplay.height;
    }

    protected final void setMaxCanvasSize(int width, int height) {
        if (Client.instance.isStretchedEnabled() && Client.instance.isResized()) {
            return;
        }

        if (maxCanvasWidth != width || height != maxCanvasHeight) {
            flagResize();
        }

        maxCanvasWidth = width;
        maxCanvasHeight = height;
    }

    /** The desktop engine posted repaints onto the AWT event queue. */
    public final void post(Object var1) {
        if (Client.instance.isGpu()) {
            DrawCallbacks drawCallbacks = Client.instance.getDrawCallbacks();
            if (drawCallbacks != null) {
                drawCallbacks.draw(viewportColor);
            }
        }
    }

    @Override
    public Canvas getCanvas() {
        return canvas;
    }

    public final void resizeCanvas() {
        if (Client.instance.isStretchedEnabled()) {
            Client.instance.invalidateStretching(false);

            if (Client.instance.isResized()) {
                com.elvarg.android.geom.Dimension realDimensions = Client.instance.getRealDimensions();
                setMaxCanvasWidth(realDimensions.width);
                setMaxCanvasHeight(realDimensions.height);
            }
        }

        if (contentWidth <= 0) {
            contentWidth = 1;
        }
        if (contentHeight <= 0) {
            contentHeight = 1;
        }

        if (Client.instance.isResized()) {
            setMaxCanvasSize(contentWidth, contentHeight);
        }

        canvasWidth = Math.min(contentWidth, maxCanvasWidth);
        canvasHeight = Math.min(contentHeight, maxCanvasHeight);
        Client.instance.getCallbacks().post(CanvasSizeChanged.INSTANCE);
        canvasX = (contentWidth - canvasWidth) / 2;
        canvasY = 0;
        canvas.setSize(canvasWidth, canvasHeight);
        AndroidDisplay.setSize(canvasWidth, canvasHeight);
        Client.rasterProvider = new ProducingGraphicsBuffer(canvasWidth, canvasHeight, canvas);

        fullRedraw = true;
        resizeGame();
    }

    protected abstract void resizeGame();

    public int keyPressed;

    @Override
    public boolean isResizeCanvasNextFrame() {
        return resizeCanvasNextFrame;
    }

    @Override
    public void setResizeCanvasNextFrame(boolean resize) {
        resizeCanvasNextFrame = resize;
    }

    @Override
    public boolean isReplaceCanvasNextFrame() {
        return isCanvasInvalid;
    }

    @Override
    public void setReplaceCanvasNextFrame(boolean replace) {
        isCanvasInvalid = replace;
    }

    @Override
    public void setMaxCanvasWidth(int width) {
        maxCanvasWidth = width;
    }

    @Override
    public void setMaxCanvasHeight(int height) {
        maxCanvasHeight = height;
    }

    @Override
    public void setFullRedraw(boolean fullRedraw) {
        this.fullRedraw = fullRedraw;
    }

    /**
     * Called by the app when the hosting view changes size. The engine then
     * resizes the canvas/raster provider on the next graphics tick, mirroring
     * how the desktop engine reacted to container resizes.
     */
    public final void setContentSize(int width, int height) {
        if (width > 0 && height > 0 && (contentWidth != width || contentHeight != height)) {
            contentWidth = width;
            contentHeight = height;
            flagResize();
        }
    }

    final void replaceCanvas() {
        addCanvas();
        flagResize();
    }

    protected final void startThread(int var1, int var2, int var3, int var4) {
        try {
            if (gameEngine != null) {
                ++threadCount;
                if (threadCount >= 3) {
                    error("alreadyloaded");
                    return;
                }
                return;
            }

            gameEngine = this;
            canvasWidth = var1;
            canvasHeight = var2;
            contentWidth = var1;
            contentHeight = var2;
            AndroidDisplay.setSize(var1, var2);
            Client.instance.getCallbacks().post(CanvasSizeChanged.INSTANCE);
            if (taskHandler == null) {
                taskHandler = new TaskHandler();
            }
            boolStartup();
        } catch (Exception ex) {
            ex.printStackTrace();
            error("crash");
        }
    }

    /** Starts the game-loop thread. Split out so lifecycle hooks stay readable. */
    private void boolStartup() {
        startRunnable(this, 1);
    }

    final synchronized void addCanvas() {
        canvasWidth = Math.max(contentWidth, 0);
        canvasHeight = Math.max(contentHeight, 0);
        if (canvasWidth <= 0) {
            canvasWidth = 765;
        }
        if (canvasHeight <= 0) {
            canvasHeight = 503;
        }
        Client.instance.getCallbacks().post(CanvasSizeChanged.INSTANCE);

        canvas = new Canvas(this);
        canvas.setSize(canvasWidth, canvasHeight);
        AndroidDisplay.setSize(canvasWidth, canvasHeight);

        if (Client.rasterProvider != null && canvasWidth == Client.rasterProvider.width && canvasHeight == Client.rasterProvider.height) {
            ((ProducingGraphicsBuffer) Client.rasterProvider).setComponent(canvas);
            Client.rasterProvider.drawFull(0, 0);
        } else {
            Client.rasterProvider = new ProducingGraphicsBuffer(canvasWidth, canvasHeight, canvas);
        }
        isCanvasInvalid = false;
        fullRedraw = true;
        field185 = method2692();
    }

    void clientTick() {
        long var1 = method2692();
        clientTickTimes[field209] = var1;
        field209 = field209 + 1 & 31;
        MouseHandler.clickMode3 = MouseHandler.instance.clickMode1;
        MouseHandler.instance.clickMode1 = 0;
        processGameLoop();
    }

    static int field4319;

    void graphicsTick() {
        long var2 = method2692();
        long var4 = graphicsTickTimes[field4319];
        graphicsTickTimes[field4319] = var2;
        field4319 = field4319 + 1 & 31;
        if (var4 != 0L && var2 > var4) {
            int var6 = (int) (var2 - var4);
            fps = ((var6 >> 1) + 32000) / var6;
        }

        if (++field199 - 1 > 50) {
            field199 -= 50;
            fullRedraw = true;
        }

        if (isCanvasInvalid) {
            replaceCanvas();
        }

        doResize();
        draw(fullRedraw);
        fullRedraw = false;
    }

    final void doResize() {
        if (resizeCanvasNextFrame) {
            resizeCanvas();
            resizeCanvasNextFrame = false;
        }
    }

    final void flagResize() {
        resizeCanvasNextFrame = true;
    }

    final synchronized void kill() {
        if (!isKilled) {
            isKilled = true;

            try {
                cleanUpForQuit();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (taskHandler != null) {
                try {
                    taskHandler.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            vmethod1099();
        }
    }

    protected abstract void startUp();

    protected abstract void processGameLoop();

    /**
     * The desktop engine returns whether the client is hosted in an AWT
     * {@code Frame}. Its {@code frame} field is never assigned in this codebase
     * (the client is launched as an applet), so this is always {@code false}
     * there; Android has no frame at all.
     */
    protected final boolean hasFrame() {
        return false;
    }

    protected abstract void vmethod1099();

    public Thread startRunnable(Runnable runnable, int i) {
        Thread thread = new Thread(runnable);
        thread.start();
        thread.setPriority(i);
        return thread;
    }

    protected abstract void draw(boolean var1);

    protected abstract void cleanUpForQuit();

    protected final void drawInitial(int percent, String text, boolean clear) {
        int width = AndroidDisplay.width;
        int height = AndroidDisplay.height;
        if (clear) {
            AndroidDisplay.clear(0);
        }
        int y = height / 2 - 18;
        AndroidDisplay.fillRect(width / 2 - 152, y, 304, 34, 0x8c1111);
        AndroidDisplay.fillRect((width / 2 - 152) + 2, y + 2, percent * 3, 30, 0x8c1111);
        AndroidDisplay.fillRect((width / 2 - 152) + 2 + percent * 3, y + 2, 300 - percent * 3, 30, 0);
    }

    protected void error(String var1) {
        if (!hasErrored) {
            hasErrored = true;
            System.out.println("error_game_" + var1);
        }
    }

    protected Bounds getFrameContentBounds() {
        return new Bounds(Math.max(contentWidth, 0), Math.max(contentHeight, 0));
    }

    /**
     * Android has no parent {@code Container} to measure; the hosting view's size
     * (kept in {@code contentWidth}/{@code contentHeight}) plays that role.
     */
    public final int getContentWidth() {
        return Math.max(contentWidth, 0);
    }

    public final int getContentHeight() {
        return Math.max(contentHeight, 0);
    }

    public static Clock getClock() {
        try {
            return new NanoClock();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new MilliClock();
        }
    }

    public void run() {
        try {
            thread = Thread.currentThread();
            thread.setName("Client");

            addCanvas();
            startUp();

            clock = getClock();

            while (0L == stopTimeMs || method2692() < stopTimeMs) {
                gameCyclesToDo = clock.wait(cycleDurationMillis, fiveOrOne);

                for (int cycles = 0; cycles < gameCyclesToDo; ++cycles) {
                    clientTick();
                }

                graphicsTick();
                post(canvas);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            error("crash");
        }

        kill();
    }

    static int gameCyclesToDo;
    static Clock clock;

    public final void start() {
        if (this == gameEngine && !isKilled) {
            stopTimeMs = 0L;
        }
    }

    /** Invoked by the app when the view gains or loses focus. */
    public final void notifyFocusChanged(boolean focused) {
        volatileFocus = focused;
        if (canvas != null) {
            canvas.setFocused(focused);
        }
        fullRedraw = true;
        final FocusChanged focusChanged = new FocusChanged();
        focusChanged.setFocused(focused);
        Client.instance.getCallbacks().post(focusChanged);
    }

    public final void stop() {
        if (this == gameEngine && !isKilled) {
            stopTimeMs = method2692() + 4000L;
        }
    }

    public static synchronized long method2692() {
        long var0 = System.currentTimeMillis();
        if (var0 < field3170) {
            field4425 += field3170 - var0;
        }

        field3170 = var0;
        return field4425 + var0;
    }

    public final void destroy() {
        if (this == gameEngine && !isKilled) {
            stopTimeMs = method2692();
            TaskUtils.sleep(5000L);
            kill();
            UserPreferences.INSTANCE.save();
        }
    }

    static long field3170;
    static long field4425;

}
