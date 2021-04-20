package ht.treechop.api;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;

public interface IChopperItem {

    int getNumChops(ItemStack tool, BlockState choppedBlockState);

}
