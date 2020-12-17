package ht.treechop.common.capabilities;

import ht.treechop.TreeChopMod;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChopSettingsProvider implements ICapabilitySerializable<NBTBase> {

    private static final String CHOP_SETTINGS_NBT = "chopSettings";
    private static final byte COMPOUND_NBT_ID = new NBTTagCompound().getId();

    @SuppressWarnings({"ConstantConditions"})
    private final ChopSettingsCapability chopSettings = ChopSettingsCapability.CAPABILITY.getDefaultInstance();

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == ChopSettingsCapability.CAPABILITY;
    }

    @SuppressWarnings({"NullableProblems", "ConstantConditions"})
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == ChopSettingsCapability.CAPABILITY) {
            return ChopSettingsCapability.CAPABILITY.cast(chopSettings);
        } else {
            return null;
        }
    }

    private ChopSettingsCapability getChopSettings() {
        return chopSettings;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTBase chopSettingsNbt = ChopSettingsCapability.CAPABILITY.writeNBT(getChopSettings(), null);
        nbt.setTag(CHOP_SETTINGS_NBT, chopSettingsNbt);
        return nbt;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void deserializeNBT(NBTBase nbt) {
        if (nbt.getId() != COMPOUND_NBT_ID) {
            TreeChopMod.LOGGER.warn("Unexpected NBT type: " + nbt);
            return;
        }
        NBTTagCompound compoundNbt = (NBTTagCompound) nbt;
        NBTBase chopSettingsNbt = compoundNbt.getTag(CHOP_SETTINGS_NBT);
        ChopSettingsCapability.CAPABILITY.readNBT(getChopSettings(), null, chopSettingsNbt);
    }

}
