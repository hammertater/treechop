package ht.treechop.common.compat;

import ht.treechop.common.event.TreeChopEvent;
import ht.treechop.common.util.TickUtil;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class NoChopOnRightClick {

    private static final Map<Entity, Long> lastRightClickTickByPlayers = new HashMap<>();

    public static void init() {
        MinecraftForge.EVENT_BUS.register(NoChopOnRightClick.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockStartClick(PlayerInteractEvent.RightClickBlock event) {
        long time = event.getWorld().getGameTime();
        lastRightClickTickByPlayers.put(event.getPlayer(), time);
    }

    @SubscribeEvent
    public static void onChop(TreeChopEvent.ChopEvent event) {
        long time = event.getWorld().getGameTime();
        if (lastRightClickTickByPlayers.getOrDefault(event.getPlayer(), TickUtil.NEVER) == time) {
            event.setCanceled(true);
        }
    }

}
