package ht.treechop.common.config;

import java.util.Arrays;

public class EnumHandle<T extends Enum<T>> extends Handle {

    private T value;

    public EnumHandle(String category, String key, T defaultValue, String comment, Class<T> enumClass) {
        super(category, key);
        String[] possibleValues = getEnumValuesAsStrings(enumClass);
        value = Enum.valueOf(enumClass, ConfigHandler.getConfig().getString(
                key, getCategory(), defaultValue.name(),
                String.format("%s\nOptions: %s", comment, String.join(", ", possibleValues)),
                possibleValues, possibleValues));
    }

    private static <T extends Enum<T>> String[] getEnumValuesAsStrings(Class<T> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    public T get() {
        return value;
    }

    void set(T value) {
        String stringValue = value.name();
        ConfigHandler.getConfig().get(getCategory(), getKey(), stringValue).set(stringValue);
        this.value = value;
        ConfigHandler.saveConfig();
    }

}
