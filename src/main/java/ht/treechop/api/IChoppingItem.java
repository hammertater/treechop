package ht.treechop.api;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

public interface IChoppingItem {

    int getNumChops(ItemStack tool, BlockState choppedBlockState);

}
