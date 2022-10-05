package ht.treechop.common;

import ht.treechop.api.ChopData;
import ht.treechop.api.TreeChopEvents;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.ChopResult;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static ht.treechop.common.util.ChopUtil.isBlockALog;

public class FabricCommon {
    public static boolean onBreakEvent(Level inLevel, Player inPlayer, BlockPos pos, BlockState blockState, BlockEntity blockEntity) {
        ItemStack tool = inPlayer.getMainHandItem();

        if (!isBlockALog(blockState)
                || !ConfigHandler.COMMON.enabled.get()
                || !(inLevel instanceof ServerLevel level)
                || !(inPlayer instanceof ServerPlayer player)
                || !ChopUtil.playerWantsToChop(player)
                || !player.hasCorrectToolForDrops(blockState)
                || !ChopUtil.canChopWithTool(tool)
        ) {
            return true;
        }

        ChopData chopData = new ChopData(ChopUtil.getNumChopsByTool(tool, blockState), ChopUtil.playerWantsToFell(player));
        if (!TreeChopEvents.BEFORE_CHOP.invoker().beforeChop(
                level,
                player,
                pos,
                blockState,
                chopData
        )) {
            return true;
        }

        ChopResult chopResult = ChopUtil.getChopResult(
                level,
                pos,
                player,
                chopData.getNumChops(),
                chopData.getFelling(),
                logPos -> isBlockALog(level, logPos)
        );

        if (chopResult != ChopResult.IGNORED) {
            if (chopResult.apply(pos, player, tool, ConfigHandler.COMMON.breakLeaves.get())) {
                if (!player.isCreative()) {
                    ChopUtil.doItemDamage(tool, level, blockState, pos, player);
                }

                return false;
            }

            TreeChopEvents.AFTER_CHOP.invoker().afterChop(level, player, pos, blockState);
        }

        return true;
    }
}