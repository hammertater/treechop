package ht.treechop.common.block;

import net.minecraft.block.state.IBlockState;

public interface IChoppable {

    IBlockState withChops(IBlockState blockState, int numChops);

    int getNumChops(IBlockState blockState);

    int getMaxNumChops();

}
