package ht.treechop.common.network;

import net.minecraftforge.event.network.CustomPayloadEvent;

@FunctionalInterface
public interface MessageHandler<T> {
    void accept(T message, CustomPayloadEvent.Context context);
}
