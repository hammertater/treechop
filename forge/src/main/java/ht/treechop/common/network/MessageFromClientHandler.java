package ht.treechop.common.network;

import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface MessageFromClientHandler<T> {
    void accept(T message, ServerPlayer sender, PacketChannel replyChannel);
}
