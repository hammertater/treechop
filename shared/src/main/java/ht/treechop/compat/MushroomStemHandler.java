package ht.treechop.compat;

import ht.treechop.api.IStrippableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MushroomStemHandler implements IStrippableBlock {
    @Override
    public BlockState getStrippedState(BlockGetter level, BlockPos pos, BlockState blockState) {
        if (PipeBlock.PROPERTY_BY_DIRECTION.values().stream().anyMatch(property -> !blockState.hasProperty(property))) {
            return blockState;
        } else {
            return blockState
                    .setValue(PipeBlock.NORTH, false)
                    .setValue(PipeBlock.EAST, false)
                    .setValue(PipeBlock.SOUTH, false)
                    .setValue(PipeBlock.WEST, false)
                    .setValue(PipeBlock.UP, false)
                    .setValue(PipeBlock.DOWN, false);
        }
    }
}
