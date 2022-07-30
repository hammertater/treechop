//package ht.treechop.common.compat;
//
//import harmonised.pmmo.api.APIUtils;
//import harmonised.pmmo.api.enums.EventType;
//import harmonised.pmmo.core.Core;
//import harmonised.pmmo.events.impl.BreakHandler;
//import ht.treechop.TreeChopMod;
//import ht.treechop.api.ChopEvent;
//import ht.treechop.common.block.ChoppedLogBlock;
//import ht.treechop.common.config.ConfigHandler;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.block.Block;
//import net.minecraft.world.level.block.Blocks;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.level.block.state.BlockState;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.level.BlockEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.LogicalSide;
//import net.minecraftforge.fml.ModList;
//import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
//import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//import net.minecraftforge.registries.ForgeRegistries;
//
//@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
//public class ProjectMMO {
//
//    @SubscribeEvent
//    public static void commonSetup(FMLCommonSetupEvent event) {
//        if (ConfigHandler.COMMON.compatForProjectMMO.get() && ModList.get().isLoaded("pmmo")) {
//            MinecraftForge.EVENT_BUS.register(EventHandler.class);
//
//            APIUtils.registerBlockXpGainTooltipData(new ResourceLocation(TreeChopMod.MOD_ID, "chopped_log"), EventType.BLOCK_BREAK, (BlockEntity entity) -> {
//                Block log = (entity instanceof ChoppedLogBlock.Entity choppedEntity) ? choppedEntity.getOriginalState().getBlock() : Blocks.OAK_LOG;
//                return Core.get(LogicalSide.SERVER).getXpUtils().getObjectExperienceMap(EventType.BLOCK_BREAK, log.getLootTable());
//            });
//        }
//    }
//
//    private static class EventHandler {
//        @SubscribeEvent
//        public static void onFinishChop(ChopEvent.FinishChopEvent event) {
//            BlockState xpBlock = (event.getLevel().getBlockEntity(event.getChoppedBlockPos()) instanceof ChoppedLogBlock.Entity entity)
//                    ? entity.getOriginalState()
//                    : event.getChoppedBlockState();
//
//            BreakHandler.handle(new BlockEvent.BreakEvent(
//                    event.getLevel(),
//                    event.getChoppedBlockPos(),
//                    xpBlock,
//                    event.getPlayer()
//            ));
//        }
//    }
//
//}
