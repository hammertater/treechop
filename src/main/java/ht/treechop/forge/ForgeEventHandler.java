package ht.treechop.forge;

import ht.treechop.util.ChopUtil;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static ht.treechop.util.ChopUtil.isBlockChoppable;

public class ForgeEventHandler {

    @SubscribeEvent
    public static void onBreakEvent(BreakEvent event) {
        if (event.getPlayer().isSneaking())
            return;

        ChopUtil.handleChopEvent(event);
    }

}
