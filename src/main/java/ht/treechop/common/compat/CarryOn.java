package ht.treechop.common.compat;

import ht.treechop.TreeChopMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import tschipp.carryon.common.handler.PickupHandler;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CarryOn {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("carryon")) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onStartChop(PickupHandler.PickUpBlockEvent event) {
            event.setCanceled(true);
        }
    }

}
