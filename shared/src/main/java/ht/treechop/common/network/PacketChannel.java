package ht.treechop.common.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

@FunctionalInterface
public interface PacketChannel {
    void send(CustomPacketPayload packet);
}
