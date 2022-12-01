package ht.treechop.client.network;

import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ForgePacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ForgeClientPacketHandler extends ForgePacketHandler {

    public static <T> void registerSender(ResourceLocation id, Class<T> packetClass, BiConsumer<T, FriendlyByteBuf> encoder) {
        HANDLER.registerMessage(id.hashCode(), packetClass, encoder, null, null);
    }

    public static <T> void registerReceiver(ResourceLocation id, Class<T> packetClass, Function<FriendlyByteBuf, T> decoder, Consumer<T> handler) {
        HANDLER.registerMessage(id.hashCode(), packetClass, null, decoder,  ServerPacketProcessor.replyChannel(handler));
    }

    public static void sendToServer(CustomPacket packet) {
        HANDLER.sendToServer(packet);
    }

    public record ServerPacketProcessor<T> (Consumer<T> handler) implements BiConsumer<T, Supplier<NetworkEvent.Context>> {
        public static <T> ServerPacketProcessor<T> replyChannel(Consumer<T> handler) {
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
