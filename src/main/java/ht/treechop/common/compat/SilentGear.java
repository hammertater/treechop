package ht.treechop.common.compat;

import ht.treechop.TreeChopMod;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class SilentGear {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForSilentGear.get() && ModList.get().isLoaded("silentgear")) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onStartChop(ChopEvent.StartChopEvent event) {
            int numChops = event.getNumChops() + ConfigHandler.COMMON.numChopsForSilentGearSaw.get() - 1;
            event.setNumChops(numChops);
        }
    }

}
