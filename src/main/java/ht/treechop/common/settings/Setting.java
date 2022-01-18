package ht.treechop.common.settings;

import net.minecraft.network.FriendlyByteBuf;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    public void encode(FriendlyByteBuf buffer) {
        field.encode(buffer, value);
    }

    public static Setting decode(FriendlyByteBuf buffer) {
        return SettingsField.decode(buffer);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } if (!(other instanceof Setting)) {
            return false;
        } else {
            Setting otherSetting = (Setting) other;
            return field == otherSetting.field && value.equals(otherSetting.value);
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31)
                .append(field)
                .append(value)
                .toHashCode();
    }
}