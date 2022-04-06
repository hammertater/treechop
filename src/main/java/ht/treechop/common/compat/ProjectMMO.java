//package ht.treechop.common.compat;
//
//import harmonised.pmmo.events.BlockBrokenHandler;
//import ht.treechop.TreeChopMod;
//import ht.treechop.api.ChopEvent;
//import ht.treechop.common.block.ChoppedLogBlock;
//import ht.treechop.common.config.ConfigHandler;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.world.BlockEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.ModList;
//import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
//import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//
//@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
//public class ProjectMMO {
//
//    @SubscribeEvent
//    public static void commonSetup(FMLCommonSetupEvent event) {
//        if (ConfigHandler.COMMON.compatForProjectMMO.get() && ModList.get().isLoaded("pmmo")) {
//            MinecraftForge.EVENT_BUS.register(EventHandler.class);
//        }
//    }
//
//    private static class EventHandler {
//        @SubscribeEvent
//        public static void onFinishChop(ChopEvent.FinishChopEvent event) {
//            BlockState xpBlock;
//            if (event.getWorld().getBlockEntity(event.getChoppedBlockPos()) instanceof ChoppedLogBlock.Entity entity) {
//                xpBlock = entity.getOriginalState();
//            } else {
//                xpBlock = event.getChoppedBlockState();
//            }
//            BlockBrokenHandler.handleBroken(new BlockEvent.BreakEvent(
//                    event.getWorld(),
//                    event.getChoppedBlockPos(),
//                    xpBlock,
//                    event.getPlayer()
//            ));
//        }
//    }
//
//}
