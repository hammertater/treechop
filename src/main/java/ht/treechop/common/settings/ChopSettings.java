package ht.treechop.common.settings;

import ht.treechop.TreeChopMod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ChopSettings {

    private EnumMap<SettingsField, Object> fieldValues = new EnumMap<>(SettingsField.class);

    public ChopSettings() {
        for (SettingsField field : SettingsField.values()) {
            fieldValues.put(field, field.getDefaultValue());
        }
    }

    public boolean getChoppingEnabled() { return get(SettingsField.CHOPPING, Boolean.class); }
    public boolean getFellingEnabled() { return get(SettingsField.FELLING, Boolean.class); }
    public SneakBehavior getSneakBehavior() { return get(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.class); }
    public boolean getTreesMustHaveLeaves() { return get(SettingsField.TREES_MUST_HAVE_LEAVES, Boolean.class); }
    public boolean getChopInCreativeMode() { return get(SettingsField.CHOP_IN_CREATIVE_MODE, Boolean.class); }

    public void setChoppingEnabled(boolean enabled) { set(SettingsField.CHOPPING, enabled); }
    public void setFellingEnabled(boolean enabled) { set(SettingsField.FELLING, enabled); }
    public void setSneakBehavior(SneakBehavior behavior) { set(SettingsField.SNEAK_BEHAVIOR, behavior); }
    public void setTreesMustHaveLeaves(boolean enabled) { set(SettingsField.TREES_MUST_HAVE_LEAVES, enabled); }
    public void setChopInCreativeMode(boolean enabled) { set(SettingsField.CHOP_IN_CREATIVE_MODE, enabled); }

    public void copyFrom(ChopSettings other) {
        fieldValues.putAll(other.fieldValues);
    }

    public <T> T get(SettingsField field, Class<T> type) {
        Object value = fieldValues.get(field);
        if (!type.isInstance(value)) {
//            TreeChopMod.LOGGER.warn(String.format("SettingsField %s has illegal value %s (%s); reverting to default", field, value, value.getClass()));
            value = field.getDefaultValue();
            fieldValues.put(field, value);
        }

        return type.cast(value);
    }

    public Object get(SettingsField field) {
        return fieldValues.get(field);
    }

    public void forEachSetting(BiConsumer<SettingsField, Object> consumer) {
        fieldValues.forEach(consumer);
    }

    public void set(SettingsField field, Object value) {
        if (field.getDefaultValue().getClass().isInstance(value)) {
            fieldValues.put(field, value);
        } else {
            TreeChopMod.LOGGER.warn(String.format("Refusing to set setting %s to illegal value %s (%s)", field, value, value.getClass()));
        }
    }

    public void set(Pair<SettingsField, Object> fieldValuePair) {
        set(fieldValuePair.getLeft(), fieldValuePair.getRight());
    }

    public void set(Setting setting) {
        set(setting.getField(), setting.getValue());
    }

    public List<Setting> getAll() {
        return Arrays.stream(SettingsField.values())
                .map(field -> new Setting(field, get(field)))
                .collect(Collectors.toList());
    }

    public Setting getSetting(SettingsField field) {
        return new Setting(field, get(field));
    }
}
