package ht.treechop;

import ht.treechop.common.Common;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public class TreeChopFabric extends TreeChop implements ModInitializer {
    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(Common::onBreakEvent);
    }
}
