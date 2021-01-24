package ht.treechop.common.compat;

import harmonised.pmmo.events.BlockBrokenHandler;
import ht.treechop.TreeChopMod;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ProjectMMO {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForProjectMMO.get() && ModList.get().isLoaded("pmmo")) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onFinishChop(ChopEvent.FinishChopEvent event) {
            BlockBrokenHandler.handleBroken(new BlockEvent.BreakEvent(
                    event.getWorld(),
                    event.getChoppedBlockPos(),
                    Blocks.OAK_LOG.getDefaultState(), // TODO: use correct wood type
                    event.getPlayer()
            ));
        }
    }

}
