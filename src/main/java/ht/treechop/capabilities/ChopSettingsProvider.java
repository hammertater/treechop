package ht.treechop.capabilities;

import ht.treechop.TreeChopMod;
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

    private LazyOptional<ChopSettings> chopSettings = LazyOptional.of(ChopSettings.CAPABILITY::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        return ChopSettings.CAPABILITY == capability ? chopSettings.cast() : LazyOptional.empty();
    }

    private ChopSettings getChopSettings() {
        return chopSettings.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty"));
    }

    @Override
    public INBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        INBT chopSettingsNbt = ChopSettings.CAPABILITY.writeNBT(getChopSettings(), null);
        nbt.put(CHOP_SETTINGS_NBT, chopSettingsNbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt.getId() != COMPOUND_NBT_ID) {
            TreeChopMod.LOGGER.warn("Unexpected NBT type: " + nbt);
            return;
        }
        CompoundNBT compoundNbt = (CompoundNBT) nbt;
        INBT chopSettingsNbt = compoundNbt.get(CHOP_SETTINGS_NBT);
        ChopSettings.CAPABILITY.readNBT(getChopSettings(), null, chopSettingsNbt);
    }

}
