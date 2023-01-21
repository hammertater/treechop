package ht.treechop.compat;

import ht.treechop.api.ISimpleChoppableBlock;
import ht.treechop.api.IStrippableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MushroomStemHandler implements ISimpleChoppableBlock, IStrippableBlock {
    @Override
    public int getRadius(BlockGetter level, BlockPos blockPos, BlockState blockState) {
        return 2;
    }

    @Override
    public int getNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
        return 0;
    }

    @Override
    public BlockState getStrippedState(BlockGetter arg0, BlockPos arg1, BlockState arg2) {
        return Blocks.STRIPPED_BIRCH_LOG.defaultBlockState();
    }

    @Override
    public int getMaxNumChops(BlockGetter level, BlockPos pos, BlockState blockState) {
        return ISimpleChoppableBlock.super.getMaxNumChops(level, pos, blockState);
    }
}
