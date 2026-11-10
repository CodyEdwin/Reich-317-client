package com.elvarg.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.elvarg.android.rl.AndroidPluginHost;

import net.runelite.client.config.ConfigBase;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.config.Range;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.plugins.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Builds the Android settings screens for RuneLite configs.
 *
 * <p>The desktop client renders these with Swing plugin panels and
 * {@code ConfigManager}-backed proxies. Android has no Swing and uses explicit
 * {@link ConfigBase} subclasses, so this editor reflects over the
 * {@link ConfigItem}-annotated accessors and creates native widgets:
 * check boxes for booleans, seek bars (or text fields) for numbers, text fields
 * for strings and spinners for enums. Values are written back through
 * {@link ConfigManager} on the game thread, exactly like a plugin would.
 */
public final class ConfigEditor {

    private ConfigEditor() {
    }

    public static void show(Activity activity, AndroidPluginHost host) {
        ConfigManager configManager = host.getConfigManager();
        List<ConfigBase> configs = new ArrayList<>();
        for (Object value : configManager.getConfigs().values()) {
            if (value instanceof ConfigBase) {
                configs.add((ConfigBase) value);
            }
        }

        if (configs.isEmpty()) {
            Toast.makeText(activity, R.string.menu_no_settings, Toast.LENGTH_SHORT).show();
            return;
        }
        if (configs.size() == 1) {
            showConfig(activity, host, configs.get(0));
            return;
        }

        String[] names = new String[configs.size()];
        for (int i = 0; i < configs.size(); i++) {
            names[i] = displayName(activity, host, configs.get(i));
        }
        new AlertDialog.Builder(activity)
                .setTitle(R.string.menu_settings)
                .setItems(names, (dialog, which) -> showConfig(activity, host, configs.get(which)))
                .setNegativeButton(R.string.menu_close, null)
                .show();
    }

    private static void showConfig(Activity activity, AndroidPluginHost host, ConfigBase config) {
        LinearLayout content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(activity, 20);
        content.setPadding(padding, dp(activity, 8), padding, dp(activity, 8));

        List<Method> items = configItems(config);
        if (items.isEmpty()) {
            TextView empty = new TextView(activity);
            empty.setText(R.string.menu_no_settings);
            content.addView(empty);
        }
        for (Method method : items) {
            addItem(activity, host, config, method, content);
        }

        ScrollView scroll = new ScrollView(activity);
        scroll.addView(content);

        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.menu_settings) + " - " + displayName(activity, host, config))
                .setView(scroll)
                .setNeutralButton(R.string.menu_reset, (dialog, which) ->
                        ClientEngine.post(() -> host.getConfigManager().resetGroup(config.group())))
                .setPositiveButton(R.string.menu_close, null)
                .show();
    }

    // ---- Widget creation --------------------------------------------------

    private static void addItem(Activity activity, AndroidPluginHost host, ConfigBase config,
                                Method method, LinearLayout parent) {
        ConfigItem item = method.getAnnotation(ConfigItem.class);
        Object value;
        try {
            value = method.invoke(config);
        } catch (ReflectiveOperationException | RuntimeException e) {
            System.err.println("ConfigEditor: cannot read " + method + ": " + e);
            return;
        }

        Class<?> type = method.getReturnType();
        View view;
        if (item.color()) {
            view = colorEditor(activity, host, config, item,
                    value instanceof Number ? ((Number) value).intValue() : 0xFFFFFFFF);
        } else if (type == boolean.class || type == Boolean.class) {
            CheckBox checkBox = new CheckBox(activity);
            checkBox.setText(item.name());
            checkBox.setChecked(Boolean.TRUE.equals(value));
            checkBox.setOnCheckedChangeListener((button, checked) ->
                    setValue(host, config, item.keyName(), Boolean.toString(checked)));
            view = checkBox;
        } else if (type == int.class || type == Integer.class) {
            view = intEditor(activity, host, config, item, method, value instanceof Number ? ((Number) value).intValue() : 0);
        } else if (type == double.class || type == Double.class) {
            view = decimalEditor(activity, host, config, item, value instanceof Number ? ((Number) value).doubleValue() : 0d);
        } else if (type == String.class) {
            view = stringEditor(activity, host, config, item, value == null ? "" : value.toString());
        } else if (type.isEnum()) {
            view = enumEditor(activity, host, config, item, type, value);
        } else if (Keybind.class.isAssignableFrom(type)) {
            view = keybindEditor(activity, host, config, item,
                    value instanceof Keybind ? (Keybind) value : Keybind.NOT_SET);
        } else {
            TextView unsupported = new TextView(activity);
            unsupported.setText(item.name() + ": " + value);
            view = unsupported;
        }

        parent.addView(view);
        if (!item.description().isEmpty()) {
            TextView description = new TextView(activity);
            description.setText(item.description());
            description.setTextSize(12f);
            description.setTextColor(0xFF9E9E9E);
            description.setPadding(0, 0, 0, dp(activity, 12));
            parent.addView(description);
        } else {
            parent.addView(spacer(activity, 12));
        }
    }

    private static View intEditor(Activity activity, AndroidPluginHost host, ConfigBase config,
                                  ConfigItem item, Method method, int value) {
        Range range = method.getAnnotation(Range.class);
        if (range == null || range.max() <= range.min()) {
            return numericEditor(activity, item, String.valueOf(value),
                    InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED, true,
                    text -> setValue(host, config, item.keyName(), text));
        }

        int min = range.min();
        int max = range.max();
        LinearLayout group = new LinearLayout(activity);
        group.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(activity);
        label.setText(item.name() + ": " + value);
        group.addView(label);

        SeekBar seekBar = new SeekBar(activity);
        seekBar.setMax(max - min);
        seekBar.setProgress(Math.max(0, Math.min(max - min, value - min)));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int current = value;

            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                current = min + progress;
                label.setText(item.name() + ": " + current);
            }

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                setValue(host, config, item.keyName(), Integer.toString(current));
            }
        });
        group.addView(seekBar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return group;
    }

    private static View decimalEditor(Activity activity, AndroidPluginHost host, ConfigBase config,
                                      ConfigItem item, double value) {
        return numericEditor(activity, item, String.valueOf(value),
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED,
                true, text -> setValue(host, config, item.keyName(), text));
    }

    private static View stringEditor(Activity activity, AndroidPluginHost host, ConfigBase config,
                                     ConfigItem item, String value) {
        return numericEditor(activity, item, value, InputType.TYPE_CLASS_TEXT, false,
                text -> setValue(host, config, item.keyName(), text));
    }

    private interface TextConsumer {
        void accept(String text);
    }

    private static View numericEditor(Activity activity, ConfigItem item, String value,
                                      int inputType, boolean numeric, TextConsumer consumer) {
        LinearLayout group = new LinearLayout(activity);
        group.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(activity);
        label.setText(item.name());
        group.addView(label);

        EditText editText = new EditText(activity);
        editText.setSingleLine(true);
        editText.setText(value);
        editText.setInputType(inputType);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();
                if (numeric && !text.isEmpty() && !text.equals("-") && !text.equals(".")) {
                    try {
                        Double.parseDouble(text);
                    } catch (NumberFormatException e) {
                        return;
                    }
                }
                consumer.accept(text);
            }
        });
        group.addView(editText);
        return group;
    }

    private static View enumEditor(Activity activity, AndroidPluginHost host, ConfigBase config,
                                   ConfigItem item, Class<?> type, Object value) {
        Object[] constants = type.getEnumConstants();
        if (constants == null || constants.length == 0) {
            TextView empty = new TextView(activity);
            empty.setText(item.name());
            return empty;
        }

        LinearLayout group = new LinearLayout(activity);
        group.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(activity);
        label.setText(item.name());
        group.addView(label);

        String[] names = new String[constants.length];
        int selected = 0;
        for (int i = 0; i < constants.length; i++) {
            names[i] = ((Enum<?>) constants[i]).name();
            if (constants[i].equals(value)) {
                selected = i;
            }
        }

        Spinner spinner = new Spinner(activity);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                activity, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(selected, false);
        final int initial = selected;
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position != initial) {
                    setValue(host, config, item.keyName(), names[position]);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        group.addView(spinner);
        return group;
    }

    private static View keybindEditor(Activity activity, AndroidPluginHost host, ConfigBase config,
                                      ConfigItem item, Keybind keybind) {
        LinearLayout group = new LinearLayout(activity);
        group.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(activity);
        label.setText(item.name());
        group.addView(label);

        Button button = new Button(activity);
        button.setAllCaps(false);
        button.setText(keybind == null ? Keybind.NOT_SET.toString() : keybind.toString());
        button.setOnClickListener(v -> captureKeybind(activity, host, config, item, button));
        group.addView(button);

        return group;
    }

    /** Asks the user to press the key that should trigger the setting. */
    private static void captureKeybind(Activity activity, AndroidPluginHost host, ConfigBase config,
                                       ConfigItem item, Button button) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.menu_keybind_prompt))
                .setOnKeyListener((dialog, keyCode, event) -> {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) {
                        return false;
                    }
                    int code = KeyMap.toAwtKeyCode(event);
                    if (code == 0) {
                        return false;
                    }
                    Keybind keybind = new Keybind(code, KeyMap.toAwtModifiers(event.getMetaState()));
                    setValue(host, config, item.keyName(),
                            keybind.getKeyCode() + ":" + keybind.getModifiers());
                    button.setText(keybind.toString());
                    dialog.dismiss();
                    return true;
                })
                .setNegativeButton(R.string.menu_close, null)
                .show();
    }

    // ---- Config access ----------------------------------------------------

    private static View colorEditor(Activity activity, AndroidPluginHost host, ConfigBase config,
                                    ConfigItem item, int value) {
        LinearLayout group = new LinearLayout(activity);
        group.setOrientation(LinearLayout.VERTICAL);

        TextView label = new TextView(activity);
        label.setText(item.name());
        group.addView(label);

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        View swatch = new View(activity);
        swatch.setBackgroundColor(value);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(dp(activity, 40), dp(activity, 40));
        swatchParams.setMargins(0, 0, dp(activity, 8), 0);
        row.addView(swatch, swatchParams);

        EditText editText = new EditText(activity);
        editText.setSingleLine(true);
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setText(String.format("%06X", value & 0xFFFFFF));
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (text.startsWith("#")) {
                    text = text.substring(1);
                }
                if (text.length() != 6) {
                    return;
                }
                try {
                    int color = 0xFF000000 | Integer.parseInt(text, 16);
                    swatch.setBackgroundColor(color);
                    setColorValue(host, config, item.keyName(), color);
                } catch (NumberFormatException e) {
                    // Ignore partially typed values.
                }
            }
        });
        row.addView(editText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        group.addView(row);
        return group;
    }

    private static void setColorValue(AndroidPluginHost host, ConfigBase config, String key, int argb) {
        ClientEngine.post(() -> host.getConfigManager().setConfiguration(
                config.group(), key, String.format("%08X", argb & 0xFFFFFFFFL)));
    }

    private static void setValue(AndroidPluginHost host, ConfigBase config, String key, String value) {
        ClientEngine.post(() -> host.getConfigManager().setConfiguration(config.group(), key, value));
    }

    /** Public, annotated, no-argument accessors, ordered by {@link ConfigItem#position()}. */
    private static List<Method> configItems(ConfigBase config) {
        List<Method> items = new ArrayList<>();
        for (Method method : config.getClass().getMethods()) {
            ConfigItem item = method.getAnnotation(ConfigItem.class);
            if (item == null || item.hidden() || method.getParameterCount() != 0
                    || Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            items.add(method);
        }
        items.sort(Comparator
                .comparingInt((Method m) -> m.getAnnotation(ConfigItem.class).position())
                .thenComparing(m -> m.getAnnotation(ConfigItem.class).name()));
        return items;
    }

    /**
     * The display name of a config: the plugin whose static CONFIG_GROUP key
     * matches the config's group, or the config class name.
     */
    private static String displayName(Activity activity, AndroidPluginHost host, ConfigBase config) {
        if (config instanceof RuneLiteConfig) {
            return activity.getString(R.string.menu_client_settings);
        }
        String group = config.group();
        for (Plugin plugin : host.getPluginManager().getPlugins()) {
            if (group.equals(pluginConfigGroup(plugin))) {
                return plugin.getName();
            }
        }
        return config.getClass().getSimpleName();
    }

    private static String pluginConfigGroup(Plugin plugin) {
        for (Field field : plugin.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && field.getType() == String.class
                    && field.getName().contains("CONFIG_GROUP")) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(null);
                    return value instanceof String ? (String) value : null;
                } catch (ReflectiveOperationException | RuntimeException e) {
                    return null;
                }
            }
        }
        return null;
    }

    // ---- Small helpers ----------------------------------------------------

    private static View spacer(Activity activity, int heightDp) {
        View view = new View(activity);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(activity, heightDp)));
        return view;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
