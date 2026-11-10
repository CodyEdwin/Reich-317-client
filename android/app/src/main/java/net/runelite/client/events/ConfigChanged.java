package net.runelite.client.events;

/**
 * Posted when a configuration entry changes.
 *
 * <p>Android variant of RuneLite's event: it drops the {@code profile} field so
 * it does not depend on {@code RuneScapeProfile} (the Android client has no
 * profile manager).
 */
public class ConfigChanged {

    private String group;
    private String key;
    private String oldValue;
    private String newValue;

    public ConfigChanged() {
    }

    public ConfigChanged(String group, String key, String oldValue, String newValue) {
        this.group = group;
        this.key = key;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    @Override
    public String toString() {
        return "ConfigChanged[group=" + group + ",key=" + key + ",old=" + oldValue + ",new=" + newValue + "]";
    }
}
