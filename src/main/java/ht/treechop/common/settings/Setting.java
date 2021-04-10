package ht.treechop.common.settings;

import net.minecraft.network.PacketBuffer;

public class Setting {

    private final SettingsField field;
    private Object value;

    Setting(SettingsField field) {
        this.field = field;
        this.value = field.getDefaultValue();
    }

    public Setting(SettingsField field, Object value) {
        this.field = field;
        this.value = value;
    }

    public SettingsField getField() {
        return field;
    }

    public Object getValue() {
        return value;
    }

    public void set(Object newValue) {
        if (newValue.getClass().isInstance(value)) {
            value = newValue;
        } else {
            throw new IllegalArgumentException(String.format("SettingsField %s cannot accept value %s (%s)", field, newValue, newValue.getClass()));
        }
    }

    public void encode(PacketBuffer buffer) {
        field.encode(buffer, value);
    }

    public static Setting decode(PacketBuffer buffer) {
        return SettingsField.decode(buffer);
    }

}