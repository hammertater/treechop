package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IChoppingItem {
    /**
     * @return {@code true} if {@code player} should chop the targeted block instead of breaking it.
     */
    boolean canChop(Player player, ItemStack tool, Level level, BlockPos pos, BlockState target);

    /**
     * @return the number of chops to perform when breaking a block. The default is 1.
     */
    int getNumChops(ItemStack tool, BlockState target);

}
