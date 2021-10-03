package ht.treechop.common.compat;

import ht.treechop.TreeChopMod;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import ht.treechop.common.event.CompatRegistrationEvent;
import ht.treechop.common.util.TickUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID)
public class NoChopRecursion {

    static private Map<EntityPlayer, Long> lastChopTickByPlayers = new HashMap<>();

    @SubscribeEvent
    public static void commonSetup(CompatRegistrationEvent event) {
        if (ConfigHandler.COMMON.preventChopRecursion.get()) {
            MinecraftForge.EVENT_BUS.register(NoChopRecursion.EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onChop(ChopEvent.StartChopEvent event) {
            EntityPlayer agent = event.getPlayer();
            long time = event.getWorld().getTotalWorldTime();
            if (lastChopTickByPlayers.getOrDefault(agent, TickUtil.NEVER) == time) {
                event.setCanceled(true);
            } else {
                lastChopTickByPlayers.put(agent, time);
            }
        }
    }

}
