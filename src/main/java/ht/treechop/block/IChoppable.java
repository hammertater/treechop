package ht.treechop.block;

import net.minecraft.block.BlockState;

public interface IChoppable {

    BlockState withChops(BlockState blockState, int numChops);

    int getNumChops(BlockState blockState);

    int getMaxNumChops();

}
