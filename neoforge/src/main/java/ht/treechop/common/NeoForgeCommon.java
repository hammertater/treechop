package ht.treechop.common;

import ht.treechop.TreeChop;
import ht.treechop.TreeChopException;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.level.BlockEvent;


@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class NeoForgeCommon {

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        ConfigHandler.updateTags();
    }

    @SubscribeEvent
    public static void onBreakEvent(BlockEvent.BreakEvent event) {
        if (event.isCanceled()
                || !(event.getLevel() instanceof ServerLevel level)
                || !(event.getPlayer() instanceof ServerPlayer agent)) {
            return;
        }

        ItemStack tool = agent.getMainHandItem();
        BlockState blockState = event.getState();
        BlockPos pos = event.getPos();

        try {
            if (ChopUtil.chop(agent, level, pos, blockState, tool, event)) {
                event.setCanceled(true);
            }
        } catch (TreeChopException e) {
            TreeChop.cry(e);
        }
    }

}
