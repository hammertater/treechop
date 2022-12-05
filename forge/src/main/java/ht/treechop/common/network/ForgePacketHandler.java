package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.SafeClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class ForgePacketHandler {
    private static final String PROTOCOL = "7";
    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TreeChop.MOD_ID + "-channel"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    public static void registerPackets() {
        registerClientToServerPacket(
                ClientRequestSettingsPacket.ID,
                ClientRequestSettingsPacket.class,
                ClientRequestSettingsPacket::encode,
                ClientRequestSettingsPacket::decode,
                ClientRequestSettingsPacket::handle
        );

        registerServerToClientPacket(
                ServerConfirmSettingsPacket.ID,
                ServerConfirmSettingsPacket.class,
                ServerConfirmSettingsPacket::encode,
                ServerConfirmSettingsPacket::decode,
                ServerConfirmSettingsPacket::handle
        );

        registerServerToClientPacket(
                ServerPermissionsPacket.ID,
                ServerPermissionsPacket.class,
                ServerPermissionsPacket::encode,
                ServerPermissionsPacket::decode,
                ServerPermissionsPacket::handle
        );

        registerServerToClientPacket(
                ServerUpdateChopsPacket.ID,
                ServerUpdateChopsPacket.class,
                ServerUpdateChopsPacket::encode,
                ServerUpdateChopsPacket::decode,
                SafeClient::handleUpdateChopsPacket
        );
    }

    private static <T> void registerClientToServerPacket(ResourceLocation id, Class<T> packetClass, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, MessageFromClientHandler<T> handler) {
        HANDLER.registerMessage(id.hashCode(), packetClass, encoder, decoder, ClientPacketProcessor.replyChannel(handler));
    }

    private static <T> void registerServerToClientPacket(ResourceLocation id, Class<T> packetClass, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, MessageFromServerHandler<T> handler) {
        HANDLER.registerMessage(id.hashCode(), packetClass, encoder, decoder, ServerPacketProcessor.replyChannel(handler));
    }

    private record ClientPacketProcessor<T> (MessageFromClientHandler<T> handler) implements BiConsumer<T, Supplier<NetworkEvent.Context>> {
        public static <T> ClientPacketProcessor<T> replyChannel(MessageFromClientHandler<T> handler) {
            return new ClientPacketProcessor<>(handler);
        }

        @Override
        public void accept(T message, Supplier<NetworkEvent.Context> context) {
            if (context.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
                context.get().enqueueWork(() -> handler.accept(message, context.get().getSender(), reply -> HANDLER.sendTo(reply, context.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT)));
            }
            context.get().setPacketHandled(true);
        }
    }

    private record ServerPacketProcessor<T> (MessageFromServerHandler<T> handler) implements BiConsumer<T, Supplier<NetworkEvent.Context>> {
        public static <T> ServerPacketProcessor<T> replyChannel(MessageFromServerHandler<T> handler) {
            return new ServerPacketProcessor<>(handler);
        }

        @Override
        public void accept(T message, Supplier<NetworkEvent.Context> context) {
            if (context.get().getDirection().getReceptionSide() == LogicalSide.CLIENT) {
                context.get().enqueueWork(() -> handler.accept(message));
            }
            context.get().setPacketHandled(true);
        }
    }
}
