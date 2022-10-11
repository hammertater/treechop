package ht.treechop.common.capabilities;

import ht.treechop.TreeChop;
import ht.treechop.common.settings.EntityChopSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ChopSettingsCapability extends EntityChopSettings implements INBTSerializable<CompoundTag> {

    public static final Capability<ChopSettingsCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});


    public ChopSettingsCapability() {
    }

    public static LazyOptional<ChopSettingsCapability> forPlayer(Player player) {
        LazyOptional<ChopSettingsCapability> lazyCapability = player.getCapability(CAPABILITY);
        if (!lazyCapability.isPresent() && !(player instanceof FakePlayer)) {
            TreeChop.LOGGER.warn("Player " + player + " is missing chop settings");
        }

        return player.getCapability(CAPABILITY);
    }

    @Override
    public CompoundTag serializeNBT() {
        return makeSaveData();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag != null) {
            readSaveData(tag);
        } else {
            TreeChop.LOGGER.warn("Failed to read ChopSettingsCapability NBT");
        }
    }
}
