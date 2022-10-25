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
 * radius {@code getUnchoppedRadius(...)}. Slimmer logs can't be chopped as many times as full-sized logs, and
 * contribute less to the "size" of the tree. For example,
 * - a log with radius 4 can only be chopped 3 times
 * - a 10-block tall tree of logs with radius 4 will only count as 5 blocks when calculating how many chops are needed
 *   to fell the tree.
 */
public interface ISimpleChoppableBlock extends IChoppableBlock {

    /**
     * When chopped, the block will turn into a {@code treechop:chopped_log} with starting radius returned by this
     * function.
     *
     * @return an integer between 1 and 8 (default), inclusive.
     */
    int getUnchoppedRadius(BlockGetter level, BlockPos blockPos, BlockState blockState);

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
        return getUnchoppedRadius(level, pos, blockState) - 1;
    }

    @Override
    default double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState) {
        return getUnchoppedRadius(level, pos, blockState) / 8.0;
    }

}
