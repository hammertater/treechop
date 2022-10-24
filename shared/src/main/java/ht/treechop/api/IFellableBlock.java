package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IFellableBlock {

    double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState);

}
