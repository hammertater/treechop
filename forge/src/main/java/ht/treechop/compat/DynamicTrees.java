package ht.treechop.compat;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import ht.treechop.TreeChop;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.api.ChopEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class DynamicTrees {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForDynamicTrees.get() && ModList.get().isLoaded("dynamictrees")) {
            MinecraftForge.EVENT_BUS.register(EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onDetectTree(ChopEvent.DetectTreeEvent event) {
            if (event.getChoppedBlockState().getBlock() instanceof BranchBlock) {
                event.setCanceled(true);
            }
        }
    }

}
