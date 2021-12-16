package ht.treechop.api;

import net.minecraft.world.level.block.state.BlockState;

public interface IChoppableBlock {

    BlockState withChops(BlockState blockState, int numChops);

    int getNumChops(BlockState blockState);

    int getMaxNumChops();

}
