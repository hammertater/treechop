package ht.treechop.common.settings;

import ht.treechop.TreeChopMod;

import java.util.EnumMap;

public class ChopSettings {

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
            TreeChopMod.LOGGER.warn(String.format("Setting %s has illegal value %s (%s); reverting to default", field, value, value.getClass()));
            value = field.getDefaultValue();
            fieldValues.put(field, value);
        }

        return type.cast(value);
    }

    public void set(Setting field, Object value) {
        if (field.getDefaultValue().getClass().isInstance(value)) {
            fieldValues.put(field, value);
        } else {
            TreeChopMod.LOGGER.warn(String.format("Refusing to set setting %s to illegal value %s (%s)", field, value, value.getClass()));
        }
    }

}
