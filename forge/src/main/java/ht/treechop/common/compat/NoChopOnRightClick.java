package ht.treechop.common.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.config.ForgeConfigHandler;
import ht.treechop.common.util.TickUtil;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NoChopOnRightClick {

    private static final Map<Entity, Long> lastRightClickTickByPlayers = new HashMap<>();
    private static boolean enabled = false;

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ForgeConfigHandler.COMMON.preventChoppingOnRightClick.get()) {
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
