package ht.treechop.common;

import ht.treechop.TreeChop;
import ht.treechop.TreeChopException;
import ht.treechop.common.chop.ChopUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FabricCommon {
    public static boolean onBreakEvent(Level level, Player player, BlockPos pos, BlockState blockState, BlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ItemStack tool = player.getMainHandItem();

            boolean chopped;
            try {
                chopped = ChopUtil.chop(serverPlayer, serverLevel, pos, blockState, tool, null);
            } catch (TreeChopException e) {
                TreeChop.cry(e);
                chopped = false;
            }

            return !chopped;
        } else {
            return true;
        }
    }
}
