package ht.treechop.common.compat;

import harmonised.pmmo.events.BlockBrokenHandler;
import harmonised.pmmo.util.XP;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import ht.treechop.common.util.TickUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class ProjectMMO {

    public static void init() {
        if (ConfigHandler.COMMON.compatForProjectMMO.get()) {
            MinecraftForge.EVENT_BUS.register(ProjectMMO.class);
        }
    }

    @SubscribeEvent
    public static void onFinishChop(ChopEvent.FinishChopEvent event) {
        BlockBrokenHandler.handleBroken(new BlockEvent.BreakEvent(
                event.getWorld(),
                event.getChoppedBlockPos(),
                event.getChoppedBlockState(),
                event.getPlayer()
        ));
    }
}
