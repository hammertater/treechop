package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IChoppableBlock extends IFellableBlock {

    /**
     * Called when the block is chopped.
     *
     * @param player The player that is chopping this block.
     * @param level The current level.
     * @param pos Block position of the chopped block in level.
     * @param blockState The block state of the chopped block before being chopped.
     * @param felling Whether the block is also being felled. If so, refrain from changing world blockstates.
     */
    void chop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState, int numChops, boolean felling);

    int getNumChops(BlockGetter level, BlockPos pos, BlockState blockState);

    int getMaxNumChops(BlockGetter level, BlockPos blockPos, BlockState blockState);

    default boolean isChoppable(BlockGetter level, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    default double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState) {
        return 1.0;
    }
}
