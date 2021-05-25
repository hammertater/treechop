package ht.treechop.common.compat;

import ht.treechop.TreeChopMod;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import ht.treechop.common.event.CompatRegistrationEvent;
import ht.treechop.common.util.TickUtil;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID)
public class NoChopOnRightClick {

    private static final Map<Entity, Long> lastRightClickTickByPlayers = new HashMap<>();
    private static boolean enabled = false;

    @SubscribeEvent
    public static void commonSetup(CompatRegistrationEvent event) {
        if (ConfigHandler.COMMON.preventChoppingOnRightClick.get()) {
            enable();
        }
    }

    public static void enable() {
        if (!enabled) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
            enabled = true;
        }
    }

    private static class EventHandler {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onBlockStartClick(PlayerInteractEvent.RightClickBlock event) {
            long time = event.getWorld().getWorldTime();
            lastRightClickTickByPlayers.put(event.getEntityPlayer(), time);
        }

        @SubscribeEvent
        public static void onChop(ChopEvent.StartChopEvent event) {
            long time = event.getWorld().getWorldTime();
            if (lastRightClickTickByPlayers.getOrDefault(event.getPlayer(), TickUtil.NEVER) == time) {
                event.setCanceled(true);
            }
        }
    }

}
