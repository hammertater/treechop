package ht.treechop.capabilities;

import ht.treechop.TreeChopMod;
import ht.treechop.config.SneakBehavior;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class ChopSettings {
    @CapabilityInject(ChopSettings.class)
    public static Capability<ChopSettings> CAPABILITY = null;

    private boolean choppingEnabled = true;
    private boolean fellingEnabled = true;
    private SneakBehavior sneakBehavior = SneakBehavior.INVERTS;

    public ChopSettings() {}

    public boolean getChoppingEnabled() { return choppingEnabled; }
    public boolean getFellingEnabled() { return fellingEnabled; }
    public SneakBehavior getSneakBehavior() { return sneakBehavior; }

    public static void register() {
        CapabilityManager.INSTANCE.register(
                ChopSettings.class,
                new ChopSettings.Storage(),
                ChopSettings::new
        );
    }

    public static class Storage implements Capability.IStorage<ChopSettings> {

        private static final String CHOPPING_ENABLED_KEY = "choppingEnabled";
        private static final String FELLING_ENABLED_KEY = "fellingEnabled";
        private static final String SNEAK_BEHAVIOR_KEY = "sneakBehavior";

        @Nullable
        @Override
        public INBT writeNBT(Capability<ChopSettings> capability, ChopSettings instance, Direction side) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putBoolean(CHOPPING_ENABLED_KEY, instance.choppingEnabled);
            nbt.putBoolean(FELLING_ENABLED_KEY, instance.fellingEnabled);
            nbt.putString(SNEAK_BEHAVIOR_KEY, instance.sneakBehavior.getString());
            return nbt;
        }

        @Override
        public void readNBT(Capability<ChopSettings> capability, ChopSettings instance, Direction side, INBT nbt) {
            if (nbt instanceof CompoundNBT) {
                CompoundNBT compoundNbt = (CompoundNBT) nbt;
                instance.choppingEnabled = compoundNbt.getBoolean(CHOPPING_ENABLED_KEY);
                instance.fellingEnabled = compoundNbt.getBoolean(FELLING_ENABLED_KEY);
                try {
                    instance.sneakBehavior = SneakBehavior.valueOf(compoundNbt.getString(SNEAK_BEHAVIOR_KEY));
                } catch (IllegalArgumentException e) {
                    TreeChopMod.LOGGER.warn(String.format("NBT contains bad sneak behavior value \"%s\"; using default value instead", compoundNbt.getString(SNEAK_BEHAVIOR_KEY)));
                    instance.sneakBehavior = SneakBehavior.INVERTS;
                }
            } else {
                TreeChopMod.LOGGER.warn("Failed to read ChopSettings NBT");
            }
        }
    }
}
