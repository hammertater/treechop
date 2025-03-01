package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IThwackableBlock extends ITreeChopBlockBehavior {
    /**
     * Called when a block is chopped, on both client and server. Overrides the default behavior of playing a sound and spawning block break particles.
     */
    void thwack(Player thwacker, Level level, BlockPos pos, BlockState state);
}
