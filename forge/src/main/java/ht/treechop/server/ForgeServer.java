package ht.treechop.server;

import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.network.*;
import ht.treechop.common.settings.EntityChopSettings;
import ht.treechop.server.network.ForgeServerPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Bus.MOD)
public class ForgeServer extends Server {
    static {
        Server.instance = new ForgeServer();
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
        registerPackets();
    }

    private static void registerPackets() {
        // Client-to-server messages
        ForgeServerPacketHandler.registerReceiver(
                ClientRequestSettingsPacket.ID,
                ClientRequestSettingsPacket.class,
                ClientRequestSettingsPacket::decode,
                ClientRequestSettingsPacket::handle);

        // Server-to-client messages
        ForgeServerPacketHandler.registerSender(
                ServerConfirmSettingsPacket.ID,
                ServerConfirmSettingsPacket.class,
                ServerConfirmSettingsPacket::encode);

        ForgeServerPacketHandler.registerSender(
                ServerPermissionsPacket.ID,
                ServerPermissionsPacket.class,
                ServerPermissionsPacket::encode);

        ForgeServerPacketHandler.registerSender(
                ServerUpdateChopsPacket.ID,
                ServerUpdateChopsPacket.class,
                ServerUpdateChopsPacket::encode);
    }

    @Override
    public void broadcast(ServerLevel level, BlockPos pos, CustomPacket packet) {
        ForgeServerPacketHandler.broadcast(level, pos, packet);
    }

    @Override
    public void sendTo(ServerPlayer player, CustomPacket packet) {
        ForgeServerPacketHandler.sendToClient(player, packet);
    }

    @Override
    public EntityChopSettings getPlayerChopSettings(Player player) {
        return ChopSettingsCapability.forPlayer(player).map(x -> (EntityChopSettings) x).orElse(Server.getDefaultPlayerSettings());
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onPlayerCloned(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                Player oldPlayer = event.getOriginal();
                Player newPlayer = event.getEntity();
                LazyOptional<ChopSettingsCapability> lazyOldSettings = ChopSettingsCapability.forPlayer(oldPlayer);
                LazyOptional<ChopSettingsCapability> lazyNewSettings = ChopSettingsCapability.forPlayer(newPlayer);

                lazyOldSettings.ifPresent(
                        oldSettings -> lazyNewSettings.ifPresent(
                                newSettings -> newSettings.copyFrom(oldSettings)
                        )
                );
            }
        }
    }
}
