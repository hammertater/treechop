package ht.treechop.server.network;

import ht.treechop.common.network.PacketChannel;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MessageFromClientHandler<T> {
    void accept(T message, ServerPlayer sender, PacketChannel replyChannel);
}
