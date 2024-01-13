package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
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
public class NoChopRecursion {

    static private final Map<Player, Long> lastChopTickByPlayers = new HashMap<>();

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.preventChopRecursion.get()) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onChop(ChopEvent.StartChopEvent event) {
            Player agent = event.getPlayer();
            long time = event.getLevel().getGameTime();
            if (lastChopTickByPlayers.getOrDefault(agent, TickUtil.NEVER) == time) {
                event.setCanceled(true);
            } else {
                lastChopTickByPlayers.put(agent, time);
            }
        }
    }

}
