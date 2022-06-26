package ht.treechop.api;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IChoppableBlock {

    /**
     * Called when the block is chopped.
     *
     * @param player The player that is chopping this block.
     * @param level The current level.
     * @param pos Block position of the chopped block in level.
     * @param blockState The block state of the chopped block before being chopped.
     * @param felling Whether the block is also being felled. If so, refrain from changing world blockstates.
     */
    void chop(PlayerEntity player, ItemStack tool, World level, BlockPos pos, BlockState blockState, int numChops, boolean felling);

    int getNumChops(World level, BlockPos pos, BlockState blockState);

    int getMaxNumChops(World level, BlockPos blockPos, BlockState blockState);

}
