package ht.treechop.common.settings;

class ChopSettingsFieldValue {

    private final Setting field;
    private Object value;

    ChopSettingsFieldValue(Setting field) {
        this.field = field;
        this.value = field.getDefaultValue();
    }

    public Object get() {
        return value;
    }

    public void set(Object newValue) {
        if (newValue.getClass().isInstance(value)) {
            value = newValue;
        } else {
            throw new IllegalArgumentException(String.format("Setting %s cannot accept value %s (%s)", field, newValue, newValue.getClass()));
        }
    }

}