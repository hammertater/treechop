package ht.treechop.common.compat;

import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import ht.treechop.common.util.TickUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class NoChopRecursion {

    static private Map<PlayerEntity, Long> lastChopTickByPlayers = new HashMap<>();

    public static void init() {
        if (ConfigHandler.COMMON.preventChopRecursion.get()) {
            MinecraftForge.EVENT_BUS.register(NoChopRecursion.class);
        }
    }

    @SubscribeEvent
    public static void onChop(ChopEvent.StartChopEvent event) {
        PlayerEntity agent = event.getPlayer();
        long time = event.getWorld().getGameTime();
        if (lastChopTickByPlayers.getOrDefault(agent, TickUtil.NEVER) == time) {
            event.setCanceled(true);
        } else {
            lastChopTickByPlayers.put(agent, time);
        }
    }
}
