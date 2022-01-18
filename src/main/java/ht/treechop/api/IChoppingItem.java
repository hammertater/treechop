package ht.treechop.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public interface IChoppingItem {

    int getNumChops(ItemStack tool, BlockState choppedBlockState);

}
