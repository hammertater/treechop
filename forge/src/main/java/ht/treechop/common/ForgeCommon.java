package ht.treechop.common;

import ht.treechop.TreeChop;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = TreeChop.MOD_ID, bus = EventBusSubscriber.Bus.FORGE)
public class ForgeCommon {

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

        boolean skip = MinecraftForge.EVENT_BUS.post(new InternalChopEvent.PreChopEvent(level, agent, pos));
        if (skip || ChopUtil.chop(agent, level, pos, blockState, tool, event)) {
            event.setCanceled(true);
        }
    }

}
