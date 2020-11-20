package ht.treechop.forge;

import ht.treechop.block.ChoppedLog;
import ht.treechop.util.ChopUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static ht.treechop.util.ChopUtil.isBlockChoppable;

public class ForgeEventHandler {

    @SubscribeEvent
    public void onBreakEvent(BreakEvent event) {
        BlockState blockState = event.getState();
        IWorld world = event.getWorld();
        BlockPos blockPos = event.getPos();

        if (event.getPlayer().isSneaking())
            return;

        int numChops;
        if (!(blockState.getBlock() instanceof ChoppedLog) && isBlockChoppable(world, blockPos, blockState)) {
            blockState = ChopUtil.chipBlock(world, blockPos, 1, event.getPlayer());
            numChops = 0;
        }
        else {
            numChops = 1;
        }

        if (blockState.getBlock() instanceof ChoppedLog) {
            ChoppedLog block = (ChoppedLog) blockState.getBlock();
            block.chop(world, blockPos, blockState, event.getPlayer(), numChops);
            event.setCanceled(true);
        }
    }

}
