package ht.treechop.common.capabilities;

import ht.treechop.TreeChopMod;
import ht.treechop.common.config.SneakBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;
import java.util.Optional;

public class ChopSettingsCapability extends ChopSettings {
    @CapabilityInject(ChopSettingsCapability.class)
    public static final Capability<ChopSettingsCapability> CAPABILITY = null;

    private boolean isSynced = false;

    public ChopSettingsCapability() {}

    public boolean isSynced() { return isSynced; }
    public void setSynced() { this.isSynced = true; }

    public static void register() {
        CapabilityManager.INSTANCE.register(
                ChopSettingsCapability.class,
                new ChopSettingsCapability.Storage(),
                ChopSettingsCapability::new
        );
    }

    @SuppressWarnings("ConstantConditions")
    public static LazyOptional<ChopSettingsCapability> forPlayer(PlayerEntity player) {
        LazyOptional<ChopSettingsCapability> lazyCapability = player.getCapability(CAPABILITY);
        if (!lazyCapability.isPresent()) {
            TreeChopMod.LOGGER.warn("Player " + player + " is missing chop settings");
        }

        return player.getCapability(CAPABILITY);
    }

    public static class Storage implements Capability.IStorage<ChopSettingsCapability> {

        private static final String CHOPPING_ENABLED_KEY = "choppingEnabled";
        private static final String FELLING_ENABLED_KEY = "fellingEnabled";
        private static final String SNEAK_BEHAVIOR_KEY = "sneakBehavior";
        private static final String TREES_MUST_HAVE_LEAVES_KEY = "treesMustHaveLeaves";
        private static final String CHOP_IN_CREATIVE_MODE_KEY = "chopInCreativeMode";
        private static final String IS_SYNCED_KEY = "isSynced";

        @Nullable
        @Override
        public INBT writeNBT(Capability<ChopSettingsCapability> capability, ChopSettingsCapability instance, Direction side) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putBoolean(CHOPPING_ENABLED_KEY, instance.getChoppingEnabled());
            nbt.putBoolean(FELLING_ENABLED_KEY, instance.getFellingEnabled());
            nbt.putString(SNEAK_BEHAVIOR_KEY, instance.getSneakBehavior().name());
            nbt.putBoolean(TREES_MUST_HAVE_LEAVES_KEY, instance.getTreesMustHaveLeaves());
            nbt.putBoolean(CHOP_IN_CREATIVE_MODE_KEY, instance.getChopInCreativeMode());
            nbt.putBoolean(IS_SYNCED_KEY, instance.isSynced());
            return nbt;
        }

        @Override
        public void readNBT(Capability<ChopSettingsCapability> capability, ChopSettingsCapability instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT) {
                CompoundNBT compoundNbt = (CompoundNBT) nbt;
                Optional<Boolean> choppingEnabled = getBoolean(compoundNbt, CHOPPING_ENABLED_KEY);
                Optional<Boolean> fellingEnabled = getBoolean(compoundNbt, FELLING_ENABLED_KEY);
                SneakBehavior sneakBehavior;
                try {
                    sneakBehavior = SneakBehavior.valueOf(compoundNbt.getString(SNEAK_BEHAVIOR_KEY));
                } catch (IllegalArgumentException e) {
                    TreeChopMod.LOGGER.warn(String.format("NBT contains bad sneak behavior value \"%s\"; using default value instead", compoundNbt.getString(SNEAK_BEHAVIOR_KEY)));
                    sneakBehavior = SneakBehavior.INVERT_CHOPPING;
                }
                Optional<Boolean> onlyChopTreesWithLeaves = getBoolean(compoundNbt, TREES_MUST_HAVE_LEAVES_KEY);
                Optional<Boolean> chopInCreativeMode = getBoolean(compoundNbt, CHOP_IN_CREATIVE_MODE_KEY);
                Optional<Boolean> isSynced = getBoolean(compoundNbt, IS_SYNCED_KEY);

                instance.setChoppingEnabled(choppingEnabled.orElse(instance.getChoppingEnabled()));
                instance.setFellingEnabled(fellingEnabled.orElse(instance.getFellingEnabled()));
                instance.setSneakBehavior(sneakBehavior);
                instance.setTreesMustHaveLeaves(onlyChopTreesWithLeaves.orElse(instance.getTreesMustHaveLeaves()));
                instance.setChopInCreativeMode(chopInCreativeMode.orElse(instance.getChopInCreativeMode()));

                if (isSynced.orElse(false)) {
                    instance.setSynced();
                }
            } else {
                TreeChopMod.LOGGER.warn("Failed to read ChopSettingsCapability NBT");
            }
        }

        private Optional<Boolean> getBoolean(CompoundNBT compoundNbt, String key) {
            return (compoundNbt.contains(key))
                    ? Optional.of(compoundNbt.getBoolean(key))
                    : Optional.empty();
        }
    }
}
