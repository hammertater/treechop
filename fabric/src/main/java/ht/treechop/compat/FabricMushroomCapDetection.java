package ht.treechop.compat;

import ht.treechop.api.TreeChopEvents;
import ht.treechop.common.config.ConfigHandler;

public class FabricMushroomCapDetection extends MushroomCapDetection {
    public static void init() {
        if (ConfigHandler.COMMON.compatForMushroomStems.get()) {
            TreeChopEvents.DETECT_TREE.register(
                    (level, player, pos, state, tree) -> detectHugeShrooms(level, pos, tree)
            );
        }
    }
}
