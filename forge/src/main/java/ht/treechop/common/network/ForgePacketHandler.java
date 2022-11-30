package ht.treechop.common.network;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

// See https://github.com/Vazkii/Botania/blob/7e1d89a1d6deda7286744e3b7c55369b2cf5e533/src/main/java/vazkii/botania/common/network/PacketHandler.java
public final class ForgePacketHandler implements PacketHandler {
    private static final String PROTOCOL = "7";
    public static final SimpleChannel HANDLER = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TreeChop.MOD_ID + "-channel"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    @SuppressWarnings("UnusedAssignment")
    public static void init() {
        int id = 0;

        // Client-to-server messages
        HANDLER.registerMessage(id++, ClientRequestSettingsPacket.class, ClientRequestSettingsPacket::encode, ClientRequestSettingsPacket::decode, ClientPacketProcessor.toServer(ClientRequestSettingsPacket::handle));

        // Server-to-client messages
        HANDLER.registerMessage(id++, ServerConfirmSettingsPacket.class, ServerConfirmSettingsPacket::encode, ServerConfirmSettingsPacket::decode, ServerPacketProcessor.toClient(ServerConfirmSettingsPacket::handle));

        HANDLER.registerMessage(id++, ServerPermissionsPacket.class, ServerPermissionsPacket::encode, ServerPermissionsPacket::decode, ServerPacketProcessor.toClient(ServerPermissionsPacket::handle));

        HANDLER.registerMessage(id++, ServerUpdateChopsPacket.class, ServerUpdateChopsPacket::encode, ServerUpdateChopsPacket::decode, ServerPacketProcessor.toClient(Client::handleUpdateChopsPacket));
    }

    @FunctionalInterface
    interface ClientPacketHandler<T> {
        void accept(T message, ServerPlayer sender, PacketChannel replyChannel);
    }

    record ClientPacketProcessor<T> (ClientPacketHandler<T> handler) implements BiConsumer<T, Supplier<NetworkEvent.Context>> {
        public static <T> ClientPacketProcessor<T> toServer(ClientPacketHandler<T> handler) {
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

    record ServerPacketProcessor<T> (Consumer<T> handler) implements BiConsumer<T, Supplier<NetworkEvent.Context>> {
        public static <T> ServerPacketProcessor<T> toClient(Consumer<T> handler) {
            return new ServerPacketProcessor<T>(handler);
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
