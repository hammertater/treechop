package ht.treechop.api;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public interface IChoppingItem {

    int getNumChops(ItemStack tool, IBlockState choppedBlockState);

}
