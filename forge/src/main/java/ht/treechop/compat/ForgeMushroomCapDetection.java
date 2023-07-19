package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopEvent;
import ht.treechop.api.TreeData;
import ht.treechop.common.config.ConfigHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = TreeChop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeMushroomCapDetection extends MushroomCapDetection {
    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        if (ConfigHandler.COMMON.compatForMushroomStems.get()) {
            MinecraftForge.EVENT_BUS.register(ForgeMushroomCapDetection.EventHandler.class);
        }
    }

    private static class EventHandler {
        @SubscribeEvent
        public static void onDetectTree(ChopEvent.DetectTreeEvent event) {
            event.getTreeData().ifPresent(tree -> {
                TreeData newTree = detectHugeShrooms(event.getLevel(), event.getChoppedBlockPos(), tree);
                event.setTreeData(newTree);
            });
        }
    }
}
