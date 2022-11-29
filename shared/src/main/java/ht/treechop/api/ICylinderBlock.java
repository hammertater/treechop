package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface ICylinderBlock {
    int getRadius(BlockGetter level, BlockPos blockPos, BlockState blockState);
}
