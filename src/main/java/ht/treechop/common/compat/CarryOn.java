//package ht.treechop.common.compat;
//
//import ht.treechop.TreeChopMod;
//import ht.treechop.api.ChopEvent;
//import ht.treechop.common.config.ConfigHandler;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.ModList;
//import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
//import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//import tschipp.carryon.common.handler.PickupHandler;
//
//@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
//public class CarryOn {
//
//    @SubscribeEvent
//    public static void commonSetup(FMLCommonSetupEvent event) {
//        if (ConfigHandler.COMMON.compatForCarryOn.get() && ModList.get().isLoaded("carryon")) {
//            if (pickUpEventIsDefined()) {
//                MinecraftForge.EVENT_BUS.register(EventHandler.class);
//            } else {
//                TreeChopMod.LOGGER.info("Disabling chopping during right-click actions to fix conflicts with Carry On (please update Carry On to carryon-1.16.5-1.15.2.9 or later for better compatibility)");
//                NoChopOnRightClick.enable();
//            }
//        }
//    }
//
//    private static boolean pickUpEventIsDefined() {
//        try {
//            Class.forName("tschipp.carryon.common.handler.PickupHandler$PickUpBlockEvent");
//            return true;
//        } catch (ClassNotFoundException e) {
//            return false;
//        }
//    }
//
//    private static class EventHandler {
//        @SubscribeEvent
//        public static void onStartChop(ChopEvent.StartChopEvent event) {
//            if (event.getBreakEvent() instanceof PickupHandler.PickUpBlockEvent) {
//                event.setCanceled(true);
//                event.getBreakEvent().setCanceled(true);
//            }
//        }
//    }
//
//}
