package ht.treechop.common.network;

@FunctionalInterface
public interface PacketChannel {
    void send(CustomPacket packet);
}
