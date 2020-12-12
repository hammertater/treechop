package ht.treechop.block;

import net.minecraft.block.IBlockState;

public interface IChoppable {

    IBlockState withChops(IBlockState blockState, int numChops);

    int getNumChops(IBlockState blockState);

    int getMaxNumChops();

}
