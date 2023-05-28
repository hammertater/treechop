package ht.treechop.common.settings;

import ht.treechop.TreeChop;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ChopSettings {

    private final EnumMap<SettingsField, Object> fieldValues = new EnumMap<>(SettingsField.class);

    public ChopSettings() {
        for (SettingsField field : SettingsField.values()) {
            fieldValues.put(field, field.getDefaultValue());
        }
    }

    public ChopSettings(ChopSettings template) {
        this();
        copyFrom(template);
    }

    public boolean getChoppingEnabled() { return get(SettingsField.CHOPPING, Boolean.class); }
    public boolean getFellingEnabled() { return get(SettingsField.FELLING, Boolean.class); }
    public SneakBehavior getSneakBehavior() { return get(SettingsField.SNEAK_BEHAVIOR, SneakBehavior.class); }
    public boolean getTreesMustHaveLeaves() { return get(SettingsField.TREES_MUST_HAVE_LEAVES, Boolean.class); }
    public boolean getChopInCreativeMode() { return get(SettingsField.CHOP_IN_CREATIVE_MODE, Boolean.class); }

    public ChopSettings setChoppingEnabled(boolean enabled) {
        set(SettingsField.CHOPPING, enabled);
        return this;
    }

    public ChopSettings setFellingEnabled(boolean enabled) {
        set(SettingsField.FELLING, enabled);
        return this;
    }

    public ChopSettings setSneakBehavior(SneakBehavior behavior) {
        set(SettingsField.SNEAK_BEHAVIOR, behavior);
        return this;
    }

    public ChopSettings setTreesMustHaveLeaves(boolean enabled) {
        set(SettingsField.TREES_MUST_HAVE_LEAVES, enabled);
        return this;
    }

    public ChopSettings setChopInCreativeMode(boolean enabled) {
        set(SettingsField.CHOP_IN_CREATIVE_MODE, enabled);
        return this;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        } else if (other instanceof ChopSettings otherSettings) {
            return Arrays.stream(SettingsField.VALUES)
                    .allMatch(field -> this.get(field).equals(otherSettings.get(field)));
        } else {
            return false;
        }
    }

    public ChopSettings copyFrom(ChopSettings other) {
        fieldValues.putAll(other.fieldValues);
        return this;
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

    public Setting getSetting(SettingsField field) {
        return new Setting(field, get(field));
    }

    public Object get(SettingsField field) {
        return fieldValues.get(field);
    }

    public void forEach(BiConsumer<SettingsField, Object> consumer) {
        fieldValues.forEach(consumer);
    }

    public ChopSettings set(SettingsField field, Object value) {
        if (field.getDefaultValue().getClass().isInstance(value)) {
            fieldValues.put(field, value);
        } else {
            TreeChop.LOGGER.warn(String.format("Refusing to set setting %s to illegal value %s (%s)", field, value, value.getClass()));
        }
        return this;
    }

    public ChopSettings set(Pair<SettingsField, Object> fieldValuePair) {
        return set(fieldValuePair.getLeft(), fieldValuePair.getRight());
    }

    public ChopSettings set(Setting setting) {
        return set(setting.getField(), setting.getValue());
    }

    public List<Setting> getAll() {
        return Arrays.stream(SettingsField.values())
                .map(field -> new Setting(field, get(field)))
                .collect(Collectors.toList());
    }
}
