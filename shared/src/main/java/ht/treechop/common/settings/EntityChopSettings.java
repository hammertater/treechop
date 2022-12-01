package ht.treechop.common.settings;

import ht.treechop.TreeChop;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class EntityChopSettings extends ChopSettings {

    private boolean isSynced = false;

    public EntityChopSettings() {
        super();
    }

    public EntityChopSettings(ChopSettings template) {
        super(template);
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced() {
        this.isSynced = true;
    }

    public CompoundTag makeSaveData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(CHOPPING_ENABLED_KEY, getChoppingEnabled());
        nbt.putBoolean(FELLING_ENABLED_KEY, getFellingEnabled());
        nbt.putString(SNEAK_BEHAVIOR_KEY, getSneakBehavior().name());
        nbt.putBoolean(TREES_MUST_HAVE_LEAVES_KEY, getTreesMustHaveLeaves());
        nbt.putBoolean(CHOP_IN_CREATIVE_MODE_KEY, getChopInCreativeMode());
        nbt.putBoolean(IS_SYNCED_KEY, isSynced());
        return nbt;
    }

    public EntityChopSettings readSaveData(CompoundTag tag) {
        if (tag.contains(IS_SYNCED_KEY)) {
            Optional<Boolean> choppingEnabled = getBoolean(tag, CHOPPING_ENABLED_KEY);
            Optional<Boolean> fellingEnabled = getBoolean(tag, FELLING_ENABLED_KEY);
            Optional<Boolean> onlyChopTreesWithLeaves = getBoolean(tag, TREES_MUST_HAVE_LEAVES_KEY);
            Optional<Boolean> chopInCreativeMode = getBoolean(tag, CHOP_IN_CREATIVE_MODE_KEY);
            Optional<Boolean> isSynced = getBoolean(tag, IS_SYNCED_KEY);

            SneakBehavior defaultSneakBehavior = ConfigHandler.defaultChopSettings.get().getSneakBehavior();
            String sneakBehaviorId = (tag.contains(SNEAK_BEHAVIOR_KEY)) ? tag.getString(SNEAK_BEHAVIOR_KEY) : "";
            if (sneakBehaviorId.isEmpty()) {
                setSneakBehavior(defaultSneakBehavior);
            } else {
                SneakBehavior sneakBehavior;
                try {
                    sneakBehavior = SneakBehavior.valueOf(sneakBehaviorId);
                } catch (IllegalArgumentException e) {
                    TreeChop.LOGGER.warn(String.format("NBT contains bad sneak behavior value \"%s\"; using default value \"%s\"", tag.getString(SNEAK_BEHAVIOR_KEY), defaultSneakBehavior.name()));
                    sneakBehavior = defaultSneakBehavior;
                }
                setSneakBehavior(sneakBehavior);
            }

            setChoppingEnabled(choppingEnabled.orElse(getChoppingEnabled()));
            setFellingEnabled(fellingEnabled.orElse(getFellingEnabled()));
            setTreesMustHaveLeaves(onlyChopTreesWithLeaves.orElse(getTreesMustHaveLeaves()));
            setChopInCreativeMode(chopInCreativeMode.orElse(getChopInCreativeMode()));

            if (isSynced.orElse(false)) {
                setSynced();
            }
        }

        return this;
    }
}
