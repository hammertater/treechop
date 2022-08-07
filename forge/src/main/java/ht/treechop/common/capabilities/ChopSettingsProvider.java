package ht.treechop.common.capabilities;

import ht.treechop.TreeChopMod;
import ht.treechop.common.settings.ChopSettings;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChopSettingsProvider implements ICapabilitySerializable<Tag> {

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
    public Tag serializeNBT() {
        return getLazyChopSettings().serializeNBT();
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        if (nbt instanceof CompoundTag) {
            getLazyChopSettings().deserializeNBT((CompoundTag) nbt);
        } else {
            TreeChopMod.LOGGER.warn("Bad ChopSettings tag type: " + nbt);
        }
    }
}
