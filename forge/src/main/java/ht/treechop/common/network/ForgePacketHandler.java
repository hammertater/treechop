package ht.treechop.common.network;

import ht.treechop.TreeChop;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.SimpleChannel;

public abstract class ForgePacketHandler {
    public static final SimpleChannel HANDLER = ChannelBuilder
            .named(TreeChop.resource("channel"))
            .simpleChannel();

    public static void registerPackets() {
        registerClientToServerPacket(
                ClientRequestSettingsPacket.class,
                ClientRequestSettingsPacket.STREAM_CODEC,
                (payload, context) -> payload.handle(
                        context.getSender(),
                        reply -> context.getConnection().send(new ServerboundCustomPayloadPacket(reply))
                )
        );

        registerServerToClientPacket(
                ServerConfirmSettingsPacket.class,
                ServerConfirmSettingsPacket.STREAM_CODEC,
                (payload, context) -> payload.handle()
        );

        registerServerToClientPacket(
                ServerPermissionsPacket.class,
                ServerPermissionsPacket.STREAM_CODEC,
                (payload, context) -> payload.handle()
        );

        registerServerToClientPacket(
                ServerUpdateChopsPacket.class,
                ServerUpdateChopsPacket.STREAM_CODEC,
                (payload, context) -> payload.handle()
        );

        HANDLER.build();
    }

    private static <T> void registerClientToServerPacket(
            Class<T> packetClass,
            StreamCodec<FriendlyByteBuf, T> streamCodec,
            MessageHandler<T> handler) {
        HANDLER.messageBuilder(packetClass)
                .codec(streamCodec)
                .direction(PacketFlow.SERVERBOUND)
                .consumer(handler::accept)
                .add();
    }

    private static <T> void registerServerToClientPacket(
            Class<T> packetClass,
            StreamCodec<FriendlyByteBuf, T> streamCodec,
            MessageHandler<T> handler) {
        HANDLER.messageBuilder(packetClass)
                .codec(streamCodec)
                .direction(PacketFlow.CLIENTBOUND)
                .consumer(handler::accept)
                .add();
    }
}
