package ht.treechop.common.capabilities;

import ht.treechop.TreeChopMod;
import ht.treechop.common.settings.ChopSettings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChopSettingsProvider implements ICapabilitySerializable<INBT> {

    private static final String CHOP_SETTINGS_NBT = "chopSettings";
    private static final byte COMPOUND_NBT_ID = new CompoundNBT().getId();

    private final LazyOptional<ChopSettingsCapability> chopSettings;

    @SuppressWarnings({"NullableProblems", "ConstantConditions"})
    public ChopSettingsProvider() {
        chopSettings = LazyOptional.of(ChopSettingsCapability.CAPABILITY::getDefaultInstance);
    }

    @SuppressWarnings({"ConstantConditions"})
    public ChopSettingsProvider(ChopSettings defaults) {
        chopSettings = LazyOptional.of(() -> {
            ChopSettingsCapability settings = ChopSettingsCapability.CAPABILITY.getDefaultInstance();
            settings.copyFrom(defaults);
            return settings;
        });
    }

    @SuppressWarnings({"NullableProblems", "ConstantConditions"})
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return (ChopSettingsCapability.CAPABILITY == capability) ? chopSettings.cast() : LazyOptional.empty();
    }

    private ChopSettingsCapability getChopSettings() {
        return chopSettings.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty"));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public INBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        INBT chopSettingsNbt = ChopSettingsCapability.CAPABILITY.writeNBT(getChopSettings(), null);
        nbt.put(CHOP_SETTINGS_NBT, chopSettingsNbt);
        return nbt;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt.getId() != COMPOUND_NBT_ID) {
            TreeChopMod.LOGGER.warn("Unexpected NBT type: " + nbt);
            return;
        }
        CompoundNBT compoundNbt = (CompoundNBT) nbt;
        INBT chopSettingsNbt = compoundNbt.get(CHOP_SETTINGS_NBT);
        ChopSettingsCapability.CAPABILITY.readNBT(getChopSettings(), null, chopSettingsNbt);
    }

}
