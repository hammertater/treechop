package ht.treechop.common.config;

public class BooleanHandle extends Handle {

    private boolean value;

    public BooleanHandle(String category, String key, boolean defaultValue, String comment) {
        super(category, key);
        value = ConfigHandler.getConfig().get(getCategory(), getKey(), defaultValue, comment).getBoolean();
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        ConfigHandler.getConfig().get(getCategory(), getKey(), value).set(value);
        this.value = value;
        ConfigHandler.saveConfig();
    }

}
