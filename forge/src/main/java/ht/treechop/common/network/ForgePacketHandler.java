package ht.treechop.common.network;

import ht.treechop.TreeChop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public abstract class ForgePacketHandler {
    public static final SimpleChannel HANDLER = ChannelBuilder
            .named(TreeChop.resource("channel"))
            .simpleChannel();

    public static void registerPackets() {
        registerPacket(
                PacketFlow.SERVERBOUND,
                ClientRequestSettingsPacket.class,
                ClientRequestSettingsPacket.STREAM_CODEC,
                (payload, context) -> payload.handle(
                        context.getSender(),
                        reply -> HANDLER.send(reply, context.getConnection())
                )
        );

        registerPacket(
                PacketFlow.CLIENTBOUND,
                ServerConfirmSettingsPacket.class,
                ServerConfirmSettingsPacket.STREAM_CODEC,
                (payload, context) -> payload.handle()
        );

        registerPacket(
                PacketFlow.CLIENTBOUND,
                ServerPermissionsPacket.class,
                ServerPermissionsPacket.STREAM_CODEC,
                (payload, context) -> payload.handle()
        );

        registerPacket(
                PacketFlow.CLIENTBOUND,
                ServerUpdateChopsPacket.class,
                ServerUpdateChopsPacket.STREAM_CODEC,
                (payload, context) -> payload.handle()
        );

        HANDLER.build();
    }

    private static <T> void registerPacket(
            PacketFlow direction,
            Class<T> packetClass,
            StreamCodec<FriendlyByteBuf, T> streamCodec,
            MessageHandler<T> handler) {
        HANDLER.messageBuilder(packetClass)
                .codec(streamCodec)
                .direction(direction)
                .consumer((payload, context) -> {
                    context.enqueueWork(() -> handler.accept(payload, context));
                    context.setPacketHandled(true);
                })
                .add();
    }

    @FunctionalInterface
    private interface MessageHandler<T> {
        void accept(T message, CustomPayloadEvent.Context context);
    }
}
