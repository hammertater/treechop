package ht.treechop.common.network;

import net.minecraft.server.level.ServerPlayer;

public interface PacketHandler {
    void sendTo(ServerPlayer player, CustomPacket serverConfirmSettingsPacket);
}
