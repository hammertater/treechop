package ht.treechop.capabilities;

import ht.treechop.TreeChopMod;
import ht.treechop.config.SneakBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

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
    public static ChopSettingsCapability forPlayer(PlayerEntity player) {
        return player.getCapability(CAPABILITY).orElseThrow(() -> new IllegalArgumentException("Player missing chop settings for " + player.getScoreboardName()));
    }

    public static class Storage implements Capability.IStorage<ChopSettingsCapability> {

        private static final String CHOPPING_ENABLED_KEY = "choppingEnabled";
        private static final String FELLING_ENABLED_KEY = "fellingEnabled";
        private static final String SNEAK_BEHAVIOR_KEY = "sneakBehavior";
        private static final String IS_SYNCED_KEY = "isSynced";

        @Nullable
        @Override
        public INBT writeNBT(Capability<ChopSettingsCapability> capability, ChopSettingsCapability instance, Direction side) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putBoolean(CHOPPING_ENABLED_KEY, instance.getChoppingEnabled());
            nbt.putBoolean(FELLING_ENABLED_KEY, instance.getFellingEnabled());
            nbt.putString(SNEAK_BEHAVIOR_KEY, instance.getSneakBehavior().name());
            nbt.putBoolean(IS_SYNCED_KEY, instance.isSynced());
            return nbt;
        }

        @Override
        public void readNBT(Capability<ChopSettingsCapability> capability, ChopSettingsCapability instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT) {
                CompoundNBT compoundNbt = (CompoundNBT) nbt;
                boolean choppingEnabled = compoundNbt.getBoolean(CHOPPING_ENABLED_KEY);
                boolean fellingEnabled = compoundNbt.getBoolean(FELLING_ENABLED_KEY);
                SneakBehavior sneakBehavior;
                try {
                    sneakBehavior = SneakBehavior.valueOf(compoundNbt.getString(SNEAK_BEHAVIOR_KEY));
                } catch (IllegalArgumentException e) {
                    TreeChopMod.LOGGER.warn(String.format("NBT contains bad sneak behavior value \"%s\"; using default value instead", compoundNbt.getString(SNEAK_BEHAVIOR_KEY)));
                    sneakBehavior = SneakBehavior.INVERT_CHOPPING;
                }
                boolean isSynced = compoundNbt.getBoolean(IS_SYNCED_KEY);

                instance.setChoppingEnabled(choppingEnabled);
                instance.setFellingEnabled(fellingEnabled);
                instance.setSneakBehavior(sneakBehavior);
                if (isSynced) {
                    instance.setSynced();
                }

            } else {
                TreeChopMod.LOGGER.warn("Failed to read ChopSettingsCapability NBT");
            }
        }
    }
}
