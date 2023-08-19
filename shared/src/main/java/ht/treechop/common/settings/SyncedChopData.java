package ht.treechop.common.settings;

import ht.treechop.TreeChop;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;

public class SyncedChopData {

    protected static final String FELLING_ENABLED_KEY = "fellingEnabled";
    protected static final String CHOPPING_ENABLED_KEY = "choppingEnabled";
    protected static final String SNEAK_BEHAVIOR_KEY = "sneakBehavior";
    protected static final String TREES_MUST_HAVE_LEAVES_KEY = "treesMustHaveLeaves";
    protected static final String CHOP_IN_CREATIVE_MODE_KEY = "chopInCreativeMode";
    protected static final String IS_SYNCED_KEY = "isSynced";

    private final ChopSettings settings;
    private boolean isSynced = false;

    public SyncedChopData(ChopSettings settings) {
        this.settings = settings;
    }

    public ChopSettings getSettings() {
        return settings;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced() {
        this.isSynced = true;
    }

    public CompoundTag makeSaveData() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(CHOPPING_ENABLED_KEY, settings.getChoppingEnabled());
        nbt.putBoolean(FELLING_ENABLED_KEY, settings.getFellingEnabled());
        nbt.putString(SNEAK_BEHAVIOR_KEY, settings.getSneakBehavior().name());
        nbt.putBoolean(TREES_MUST_HAVE_LEAVES_KEY, settings.getTreesMustHaveLeaves());
        nbt.putBoolean(CHOP_IN_CREATIVE_MODE_KEY, settings.getChopInCreativeMode());
        nbt.putBoolean(IS_SYNCED_KEY, isSynced());
        return nbt;
    }

    public SyncedChopData readSaveData(CompoundTag tag) {
        if (tag.contains(IS_SYNCED_KEY)) {
            Optional<Boolean> choppingEnabled = getBoolean(tag, CHOPPING_ENABLED_KEY);
            Optional<Boolean> fellingEnabled = getBoolean(tag, FELLING_ENABLED_KEY);
            Optional<Boolean> onlyChopTreesWithLeaves = getBoolean(tag, TREES_MUST_HAVE_LEAVES_KEY);
            Optional<Boolean> chopInCreativeMode = getBoolean(tag, CHOP_IN_CREATIVE_MODE_KEY);
            Optional<Boolean> isSynced = getBoolean(tag, IS_SYNCED_KEY);

            SneakBehavior defaultSneakBehavior = ConfigHandler.defaultChopSettings.get().getSneakBehavior();
            String sneakBehaviorId = (tag.contains(SNEAK_BEHAVIOR_KEY)) ? tag.getString(SNEAK_BEHAVIOR_KEY) : "";
            if (sneakBehaviorId.isEmpty()) {
                settings.setSneakBehavior(defaultSneakBehavior);
            } else {
                SneakBehavior sneakBehavior;
                try {
                    sneakBehavior = SneakBehavior.valueOf(sneakBehaviorId);
                } catch (IllegalArgumentException e) {
                    TreeChop.LOGGER.warn(String.format("NBT contains bad sneak behavior value \"%s\"; using default value \"%s\"", tag.getString(SNEAK_BEHAVIOR_KEY), defaultSneakBehavior.name()));
                    sneakBehavior = defaultSneakBehavior;
                }
                settings.setSneakBehavior(sneakBehavior);
            }

            settings.setChoppingEnabled(choppingEnabled.orElse(settings.getChoppingEnabled()));
            settings.setFellingEnabled(fellingEnabled.orElse(settings.getFellingEnabled()));
            settings.setTreesMustHaveLeaves(onlyChopTreesWithLeaves.orElse(settings.getTreesMustHaveLeaves()));
            settings.setChopInCreativeMode(chopInCreativeMode.orElse(settings.getChopInCreativeMode()));

            if (isSynced.orElse(false)) {
                setSynced();
            }
        }

        return this;
    }

    protected Optional<Boolean> getBoolean(CompoundTag CompoundTag, String key) {
        return (CompoundTag.contains(key))
                ? Optional.of(CompoundTag.getBoolean(key))
                : Optional.empty();
    }
}
