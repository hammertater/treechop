package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Blocks that implement this interface will be replaced by {@code treechop:chopped_log} when chopped, with starting
 * radius {@code getUnchoppedRadius}, which determines the maximum number of times the block can be chopped and the amount
 * this block contributes to the "size" of the tree, e.g., felling a tree with radius 4 logs will require half the
 * number of chops of a tree with full-size (radius 8) logs.
 */
public interface ISimpleChoppableBlock extends IChoppableBlock {

    @Override
    default void chop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState, int numChops, boolean felling) {
        final ResourceLocation CHOPPED_LOG = new ResourceLocation("treechop", "chopped_log");
        Block choppedLog = Registry.BLOCK.get(CHOPPED_LOG);
        if (choppedLog instanceof IChoppableBlock choppableBlock) {
            choppableBlock.chop(player, tool, level, pos, blockState, numChops, felling);
        }
    }

    @Override
    default int getNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
        return 0;
    }

    @Override
    default int getMaxNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
        return getUnchoppedRadius(level, pos, blockState);
    }

    @Override
    default double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState) {
        return getUnchoppedRadius(level, pos, blockState) / 8.0;
    }

    /**
     * @param level
     * @param blockPos
     * @param blockState
     * @return an integer between 1 and 8 (default), inclusive.
     */
    int getUnchoppedRadius(BlockGetter level, BlockPos blockPos, BlockState blockState);

}
