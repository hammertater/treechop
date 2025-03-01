package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ILeaveslikeBlock extends ITreeChopBlockBehavior {

    /**
     * Called when the tree this block is attached to is felled.
     *
     * @param player The player that is chopping this block.
     * @param level The current level.
     * @param pos Block position of the chopped block in level.
     */
    void fell(Player player, Level level, BlockPos pos, BlockState blockState);

}
