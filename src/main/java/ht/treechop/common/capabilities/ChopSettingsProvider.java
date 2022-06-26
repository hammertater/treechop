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

    private final ChopSettingsCapability chopSettings = new ChopSettingsCapability();
    private final LazyOptional<ChopSettingsCapability> lazyChopSettings = LazyOptional.of(() -> chopSettings);

    public ChopSettingsProvider() {
    }

    public ChopSettingsProvider(ChopSettings defaults) {
        super();
        chopSettings.copyFrom(defaults);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return (ChopSettingsCapability.CAPABILITY == capability) ? lazyChopSettings.cast() : LazyOptional.empty();
    }

    private ChopSettingsCapability getLazyChopSettings() {
        return lazyChopSettings.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty"));
    }

    @Override
    public INBT serializeNBT() {
        return getLazyChopSettings().serializeNBT();
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt instanceof CompoundNBT) {
            getLazyChopSettings().deserializeNBT((CompoundNBT) nbt);
        } else {
            TreeChopMod.LOGGER.warn("Bad ChopSettings tag type: " + nbt);
        }
    }
}
