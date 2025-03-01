package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.TickUtil;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NoChopOnRightClick {

    private static final Map<Entity, Long> lastRightClickTickByPlayers = new HashMap<>();
    private static boolean enabled = false;

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.preventChoppingOnRightClick.get()) {
            enable();
        }
    }

    public static void enable() {
        if (!enabled) {
            NeoForge.EVENT_BUS.register(EventHandler.class);
            enabled = true;
        }
    }

    private static class EventHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBlockStartClick(PlayerInteractEvent.RightClickBlock event) {
            long time = event.getLevel().getGameTime();
            lastRightClickTickByPlayers.put(event.getEntity(), time);
        }

        @SubscribeEvent
        public static void onChop(ChopEvent.StartChopEvent event) {
            long time = event.getLevel().getGameTime();
            if (lastRightClickTickByPlayers.getOrDefault(event.getPlayer(), TickUtil.NEVER) == time) {
                event.setCanceled(true);
            }
        }
    }

}
