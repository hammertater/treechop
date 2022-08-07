package ht.treechop.common;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class Common {
    public static boolean onBreakEvent(Level level, Player player, BlockPos blockPos, BlockState blockState, BlockEntity blockEntity) {
        return false;
    }
}
