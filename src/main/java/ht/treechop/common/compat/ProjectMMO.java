package ht.treechop.common.compat;

import harmonised.pmmo.events.BlockBrokenHandler;
import ht.treechop.TreeChopMod;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import ht.treechop.common.event.CompatRegistrationEvent;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = TreeChopMod.MOD_ID)
public class ProjectMMO {

    @SubscribeEvent
    public static void commonSetup(CompatRegistrationEvent event) {
        if (ConfigHandler.COMMON.compatForProjectMMO.get() && Loader.isModLoaded("pmmo")) {
            MinecraftForge.EVENT_BUS.register(ProjectMMO.EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onChop(ChopEvent.FinishChopEvent event) {
            BlockBrokenHandler.handleBroken(new BlockEvent.BreakEvent(
                    event.getWorld(),
                    event.getChoppedBlockPos(),
                    Blocks.LOG.getDefaultState(), // TODO: use correct wood type
                    event.getPlayer()
            ));
        }
    }

}
