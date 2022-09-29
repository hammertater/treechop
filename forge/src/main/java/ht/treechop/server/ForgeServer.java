package ht.treechop.server;

import ht.treechop.TreeChop;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ForgePacketHandler;
import ht.treechop.common.settings.EntityChopSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = TreeChop.MOD_ID)
public class ForgeServer extends Server {
    static {
        Server.instance = new ForgeServer();
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getPlayer();
            LazyOptional<ChopSettingsCapability> lazyOldSettings = ChopSettingsCapability.forPlayer(oldPlayer);
            LazyOptional<ChopSettingsCapability> lazyNewSettings = ChopSettingsCapability.forPlayer(newPlayer);

            lazyOldSettings.ifPresent(
                    oldSettings -> lazyNewSettings.ifPresent(
                            newSettings -> newSettings.copyFrom(oldSettings)
                    )
            );
        }
    }

    @Override
    public void broadcast(ServerLevel level, BlockPos pos, CustomPacket packet) {
        ForgePacketHandler.HANDLER.send(PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(pos)), packet);
    }

    @Override
    public void sendTo(ServerPlayer player, CustomPacket packet) {
        ForgePacketHandler.HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
    }

    @Override
    public EntityChopSettings getPlayerChopSettings(Player player) {
        return ChopSettingsCapability.forPlayer(player).map(x -> (EntityChopSettings) x).orElse(Server.getDefaultPlayerSettings());
    }
}
