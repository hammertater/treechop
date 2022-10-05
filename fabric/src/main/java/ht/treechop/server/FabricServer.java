package ht.treechop.server;

import ht.treechop.common.network.ClientRequestSettingsPacket;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.settings.EntityChopSettings;
import ht.treechop.common.settings.ChoppingEntity;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

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
            EntityChopSettings chopSettings = instance.getPlayerChopSettings(oldPlayer);
            ((ChoppingEntity) newPlayer).setChopSettings(chopSettings);
        });
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ClientRequestSettingsPacket.ID, (server, player, handler, buffer, sender) -> {
            ClientRequestSettingsPacket packet = ClientRequestSettingsPacket.decode(buffer);
            server.execute(() -> ClientRequestSettingsPacket.handle(packet, player, reply -> sendTo(player, reply)));
        });
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

    @Override
    public EntityChopSettings getPlayerChopSettings(Player player) {
        ChoppingEntity chopper = (ChoppingEntity) player;
        if (chopper.getChopSettings() == null) {
            chopper.setChopSettings(new EntityChopSettings(getDefaultPlayerSettings()));
        }
        return chopper.getChopSettings();
    }
}