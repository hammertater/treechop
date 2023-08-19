package ht.treechop.server;

import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ForgePacketHandler;
import ht.treechop.common.settings.ChoppingEntity;
import ht.treechop.common.settings.SyncedChopData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(bus = Bus.MOD)
public class ForgeServer extends Server {
    static {
        Server.instance = new ForgeServer();
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
    }

    @Override
    public void broadcast(ServerLevel level, BlockPos pos, CustomPacket packet) {
        ForgePacketHandler.HANDLER.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }

    @Override
    public void sendTo(ServerPlayer player, CustomPacket packet) {
        ForgePacketHandler.HANDLER.sendTo(packet, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onPlayerCloned(PlayerEvent.Clone event) {
            if (event.isWasDeath()) {
                Player oldPlayer = event.getOriginal();
                Player newPlayer = event.getEntity();

                SyncedChopData chopSettings = instance.getPlayerChopData(oldPlayer);
                ((ChoppingEntity) newPlayer).setChopData(chopSettings);
            }
        }
    }
}
