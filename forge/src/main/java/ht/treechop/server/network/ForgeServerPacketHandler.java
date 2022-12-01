package ht.treechop.server.network;

import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ForgePacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeServerPacketHandler extends ForgePacketHandler {

    public static <T> void registerSender(ResourceLocation id, Class<T> packetClass, BiConsumer<T, FriendlyByteBuf> encoder) {
        HANDLER.registerMessage(id.hashCode(), packetClass, encoder, null, null);
    }

    public static <T> void registerReceiver(ResourceLocation id, Class<T> packetClass, Function<FriendlyByteBuf, T> decoder, MessageFromClientHandler<T> handler) {
        HANDLER.registerMessage(id.hashCode(), packetClass, null, decoder, ClientPacketProcessor.replyChannel(handler));
    }

    public static void broadcast(ServerLevel level, BlockPos pos, CustomPacket packet) {
        HANDLER.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }

    public static void sendToClient(ServerPlayer player, CustomPacket packet) {
        ForgePacketHandler.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    public record ClientPacketProcessor<T> (MessageFromClientHandler<T> handler) implements BiConsumer<T, Supplier<NetworkEvent.Context>> {
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
}
