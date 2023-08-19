package ht.treechop.server;

import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.ChoppingEntity;
import ht.treechop.common.settings.SyncedChopData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public abstract class Server {

    protected static Server instance;

    public static ChopSettings getDefaultPlayerSettings() {
        return new ChopSettings(ConfigHandler.defaultChopSettings.get());
    }

    public SyncedChopData getPlayerChopData(Player player) {
        ChoppingEntity chopper = (ChoppingEntity) player;
        if (chopper.getChopData() == null) {
            chopper.setChopData(new SyncedChopData(getDefaultPlayerSettings()));
        }
        return chopper.getChopData();
    }

    public abstract void broadcast(ServerLevel level, BlockPos pos, CustomPacket packet);

    public abstract void sendTo(ServerPlayer player, CustomPacket packet);

    public static Server instance() {
        return instance;
    }
}
