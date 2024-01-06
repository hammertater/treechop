package ht.treechop.server;

import ht.treechop.common.network.ClientRequestSettingsPacket;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.settings.ChoppingEntity;
import ht.treechop.common.settings.SyncedChopData;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;

public class FabricServer extends Server implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        // Only run on dedicated servers, not local servers!
    }

    public static void initialize() {
        FabricServer instance = new FabricServer();
        instance.registerPackets();
        Server.instance = instance;

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            SyncedChopData chopSettings = instance.getPlayerChopData(oldPlayer);
            ((ChoppingEntity) newPlayer).setChopData(chopSettings);
        });
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ClientRequestSettingsPacket.ID, (server, player, handler, buffer, sender) -> {
            ClientRequestSettingsPacket packet = ClientRequestSettingsPacket.decode(buffer);
            server.execute(() -> ClientRequestSettingsPacket.handle(packet, player, reply -> replyTo(sender, reply)));
        });
    }

    private void replyTo(PacketSender sender, CustomPacket packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        sender.sendPacket(packet.getId(), packet.encode(buffer));
    }

    @Override
    public void broadcast(ServerLevel level, BlockPos pos, CustomPacket packet) {
        for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
            instance().sendTo(player, packet);
        }
    }

    @Override
    public void sendTo(ServerPlayer player, CustomPacket packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ServerPlayNetworking.send(player, packet.getId(), packet.encode(buffer));
    }
}
