package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Used to set the radius of the block on its first chop. This can have unexpected consequences, which are accounted for
 * in the default implementations of {@link ISimpleChoppableBlock}, so prefer using that instead.
 */
public interface ICylinderBlock extends ITreeChopBlockBehavior {
    int getRadius(BlockGetter level, BlockPos blockPos, BlockState blockState);
}
