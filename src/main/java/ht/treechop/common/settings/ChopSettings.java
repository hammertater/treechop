package ht.treechop.common.settings;

import ht.treechop.TreeChopMod;
import ht.treechop.common.network.SingleSetting;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ChopSettings {

    private SingleSetting setting;

    public ChopSettings() {
        for (Setting field : Setting.values()) {
            fieldValues.put(field, new ChopSettingsFieldValue(field));
        }
    }

    public boolean getChoppingEnabled() { return get(Setting.CHOPPING, Boolean.class); }
    public boolean getFellingEnabled() { return get(Setting.FELLING, Boolean.class); }
    public SneakBehavior getSneakBehavior() { return get(Setting.SNEAK_BEHAVIOR, SneakBehavior.class); }
    public boolean getTreesMustHaveLeaves() { return get(Setting.TREES_MUST_HAVE_LEAVES, Boolean.class); }
    public boolean getChopInCreativeMode() { return get(Setting.CHOP_IN_CREATIVE_MODE, Boolean.class); }

    public void setChoppingEnabled(boolean enabled) { set(Setting.CHOPPING, enabled); }
    public void setFellingEnabled(boolean enabled) { set(Setting.FELLING, enabled); }
    public void setSneakBehavior(SneakBehavior behavior) { set(Setting.SNEAK_BEHAVIOR, behavior); }
    public void setTreesMustHaveLeaves(boolean enabled) { set(Setting.TREES_MUST_HAVE_LEAVES, enabled); }
    public void setChopInCreativeMode(boolean enabled) { set(Setting.CHOP_IN_CREATIVE_MODE, enabled); }

    public void toggleChopping() {
        setChoppingEnabled(!getChoppingEnabled());
    }

    public void toggleFelling() {
        setFellingEnabled(!getFellingEnabled());
    }

    public void cycleSneakBehavior() {
        SneakBehavior nextSneakBehavior = SneakBehavior.values()[Math.floorMod(getSneakBehavior().ordinal() + 1, SneakBehavior.values().length)];
        setSneakBehavior(nextSneakBehavior);
    }

    public void copyFrom(ChopSettings other) {
        fieldValues.putAll(other.fieldValues);
    }

    private EnumMap<Setting, Object> fieldValues = new EnumMap<>(Setting.class);

    public <T> T get(Setting field, Class<T> type) {
        Object value = fieldValues.get(field);
        if (!type.isInstance(value)) {
//            TreeChopMod.LOGGER.warn(String.format("Setting %s has illegal value %s (%s); reverting to default", field, value, value.getClass()));
            value = field.getDefaultValue();
            fieldValues.put(field, value);
        }

        return type.cast(value);
    }

    public Object get(Setting field) {
        return fieldValues.get(field);
    }

    public void forEachSetting(BiConsumer<Setting, Object> consumer) {
        fieldValues.forEach(consumer);
    }

    public void set(Setting field, Object value) {
        if (field.getDefaultValue().getClass().isInstance(value)) {
            fieldValues.put(field, value);
        } else {
            TreeChopMod.LOGGER.warn(String.format("Refusing to set setting %s to illegal value %s (%s)", field, value, value.getClass()));
        }
    }

    public void set(Pair<Setting, Object> fieldValuePair) {
        set(fieldValuePair.getLeft(), fieldValuePair.getRight());
    }

    public void set(SingleSetting setting) {
        set(setting.getField(), setting.getValue());
    }

    public List<SingleSetting> getAll() {
        return Arrays.stream(Setting.values())
                .map(field -> new SingleSetting(field, get(field)))
                .collect(Collectors.toList());
    }
}
