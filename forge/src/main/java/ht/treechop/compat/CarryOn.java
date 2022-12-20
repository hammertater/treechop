package ht.treechop.compat;

import ht.treechop.TreeChop;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class CarryOn {

//    @SubscribeEvent
//    public static void commonSetup(FMLCommonSetupEvent event) {
//        if (ModList.get().isLoaded("carryon") && CompatUtil.classExists()) {
//            MinecraftForge.EVENT_BUS.register(EventHandler.class);
//        }
//    }

    // Note: as of carryon 1.18.1.2, carryon is not receiving IMCs.
    @SubscribeEvent
    public static void enqueueModComms(InterModEnqueueEvent event) {
        InterModComms.sendTo("carryon", "blacklistBlock", () -> "treechop:chopped_log");
    }

//    private static class EventHandler {
//        @SubscribeEvent
//        public static void onStartChop(ChopEvent.StartChopEvent chopEvent) {
//            if (chopEvent.getTrigger() instanceof PickupHandler.PickUpBlockEvent breakEvent) {
//                chopEvent.setCanceled(true);
//                breakEvent.setCanceled(true);
//            }
//        }
//    }

}
