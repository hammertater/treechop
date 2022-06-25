package ht.treechop.common.compat;

import harmonised.pmmo.api.TooltipSupplier;
import harmonised.pmmo.config.JType;
import harmonised.pmmo.events.BlockBrokenHandler;
import harmonised.pmmo.util.XP;
import ht.treechop.TreeChopMod;
import ht.treechop.api.ChopEvent;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Collections;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ProjectMMO {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForProjectMMO.get() && ModList.get().isLoaded("pmmo")) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);

            TooltipSupplier.registerBreakTooltipData(new ResourceLocation(TreeChopMod.MOD_ID, "chopped_log"), JType.XP_VALUE_BREAK, (entity) -> {
                ResourceLocation log = (entity instanceof ChoppedLogBlock.Entity choppedEntity) ? choppedEntity.getOriginalState().getBlock().getRegistryName() : Blocks.OAK_LOG.getRegistryName();
                if (log != null) {
                    return XP.getXpBypass(log, JType.XP_VALUE_BREAK);
                } else {
                    return Collections.emptyMap();
                }
            });
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onFinishChop(ChopEvent.FinishChopEvent event) {
            BlockState xpBlock = (event.getWorld().getBlockEntity(event.getChoppedBlockPos()) instanceof ChoppedLogBlock.Entity entity)
                    ? entity.getOriginalState()
                    : event.getChoppedBlockState();

            BlockBrokenHandler.handleBroken(new BlockEvent.BreakEvent(
                    event.getWorld(),
                    event.getChoppedBlockPos(),
                    xpBlock,
                    event.getPlayer()
            ));
        }
    }

}
