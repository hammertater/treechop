package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface IStrippableBlock extends ITreeChopBlockBehavior {
    /**
     * Only used to change the appearance of the log when chopped
     *
     * @return a blockstate that will be inspected to find the appropriate textures.
     */
    BlockState getStrippedState(BlockGetter level, BlockPos pos, BlockState blockState);

}
