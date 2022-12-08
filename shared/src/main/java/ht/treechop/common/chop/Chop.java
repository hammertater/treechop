package ht.treechop.common.chop;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Chop {
    private final BlockPos blockPos;
    private final int numChops;

    public Chop(BlockPos blockPos, int numChops) {
        this.blockPos = blockPos;
        this.numChops = numChops;
    }

    public int getNumChops() {
        return numChops;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void apply(Level level, Player player, ItemStack tool, boolean felling) {
        BlockState blockState = level.getBlockState(blockPos);
        IChoppableBlock choppableBlock = ChopUtil.getChoppableBlock(level, blockPos, blockState);
        if (choppableBlock != null) {
            choppableBlock.chop(player, tool, level, blockPos, blockState, numChops, felling);
        } else {
            TreeChop.LOGGER.warn("Failed to chop block in level {} at position {} for player {}: {} is not choppable", level.dimension(), blockPos, player.getName(), TreeChop.platform.getResourceLocationForBlock(blockState.getBlock()));
        }
    }
}
