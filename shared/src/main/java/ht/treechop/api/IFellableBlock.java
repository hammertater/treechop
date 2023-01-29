package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IFellableBlock extends ITreeChopBlockBehavior {
    /**
     * @return the amount that this block contributes to the "size" of a tree, which determines the number of chops
     * required to fell a tree. The default is 1.0.
     */
    double getSupportFactor(BlockGetter level, BlockPos pos, BlockState blockState);

}
