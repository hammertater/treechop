package ht.treechop.common.network;

import ht.treechop.TreeChop;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
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
        HANDLER.registerMessage(id++, ClientRequestSettingsPacket.class, ClientRequestSettingsPacket::encode, ClientRequestSettingsPacket::decode, PacketProcessor.toServer(ClientRequestSettingsPacket::handle));

        // Server-to-client messages
        HANDLER.registerMessage(id++, ServerConfirmSettingsPacket.class, ServerConfirmSettingsPacket::encode, ServerConfirmSettingsPacket::decode, PacketProcessor.toClient(ServerConfirmSettingsPacket::handle));
        HANDLER.registerMessage(id++, ServerPermissionsPacket.class, ServerPermissionsPacket::encode, ServerPermissionsPacket::decode, PacketProcessor.toClient(ServerPermissionsPacket::handle));
        HANDLER.registerMessage(id++, ServerUpdateChopsPacket.class, ServerUpdateChopsPacket::encode, ServerUpdateChopsPacket::decode, PacketProcessor.toClient(ServerUpdateChopsPacket::handle));
    }

    public void sendToServer(CustomPacket packet) {
        HANDLER.sendToServer(packet);
    }

    public void sendTo(ServerPlayer playerMP, CustomPacket packet) {
         HANDLER.sendTo(packet, playerMP.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    record PacketProcessor<T> (BiConsumer<T, ServerPlayer> handler, LogicalSide receiverSide) implements BiConsumer<T, Supplier<NetworkEvent.Context>> {
        public static <T> PacketProcessor<T> toServer(BiConsumer<T, ServerPlayer> handler) {
            return new PacketProcessor<>(handler, LogicalSide.SERVER);
        }

        public static <T> PacketProcessor<T> toClient(BiConsumer<T, ServerPlayer> handler) {
            return new PacketProcessor<>(handler, LogicalSide.CLIENT);
        }

        @Override
        public void accept(T message, Supplier<NetworkEvent.Context> context) {
            if (context.get().getDirection().getReceptionSide() == receiverSide) {
                context.get().enqueueWork(() -> handler.accept(message, context.get().getSender()));
            }
            context.get().setPacketHandled(true);

        }
    }
}
