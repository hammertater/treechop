package ht.treechop.common.capabilities;

import ht.treechop.TreeChop;
import ht.treechop.common.settings.SyncedChopData;
import ht.treechop.server.Server;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ChopSettingsCapability extends SyncedChopData implements INBTSerializable<CompoundTag> {

    public static final Capability<ChopSettingsCapability> CAPABILITY = CapabilityManager.get(new CapabilityToken<>(){});

    public ChopSettingsCapability() {
        super(Server.getDefaultPlayerSettings());
    }

    public static LazyOptional<SyncedChopData> forPlayer(Player player) {
        LazyOptional<SyncedChopData> lazyCapability = player.getCapability(CAPABILITY).cast();
        if (!lazyCapability.isPresent() && !(player instanceof FakePlayer)) {
            TreeChop.LOGGER.warn("Player " + player + " is missing TreeChop data");
        }

        return player.getCapability(CAPABILITY).cast();
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
