package ht.treechop.api;

import net.minecraft.block.state.IBlockState;

public interface IChoppableBlock {

    IBlockState withChops(IBlockState blockState, int numChops);

    int getNumChops(IBlockState blockState);

    int getMaxNumChops();

}
