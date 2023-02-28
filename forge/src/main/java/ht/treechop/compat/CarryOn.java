package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.config.ConfigHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import tschipp.carryon.common.handler.PickupHandler;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CarryOn {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForCarryOn.get() && ModList.get().isLoaded("carryon") && CompatUtil.classExists("tschipp.carryon.common.handler.PickupHandler")) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onStartChop(ChopEvent.StartChopEvent chopEvent) {
            if (chopEvent.getTrigger() instanceof PickupHandler.PickUpBlockEvent breakEvent) {
                chopEvent.setCanceled(true);
                breakEvent.setCanceled(true);
            }
        }
    }

}
