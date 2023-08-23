package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.common.InternalChopEvent;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.TickUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class MultiMine {

    static private final Map<Player, Long> lastChopTickByPlayers = new HashMap<>();

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        // TODO: Check if Multi Mine is loaded
        if (ConfigHandler.COMMON.compatForMultiMine.get()) {
            MinecraftForge.EVENT_BUS.register(MultiMine.EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onChop(InternalChopEvent.PreChopEvent event) {
            Player player = event.getPlayer();
            long gameTick = event.getLevel().getGameTime();
            // TODO: Check for repeated block pos instead of player
            if (lastChopTickByPlayers.getOrDefault(player, TickUtil.NEVER) == gameTick) {
                event.setCanceled(true);
            } else {
                lastChopTickByPlayers.put(player, gameTick);
            }
        }
    }
}
