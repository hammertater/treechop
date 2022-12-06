package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IChoppingItem {

    boolean canChop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState target);

    int getNumChops(ItemStack tool, BlockState choppedBlockState);

}
