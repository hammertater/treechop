package ht.treechop.common.events;

import ht.treechop.TreeChop;
import ht.treechop.api.ChopData;
import ht.treechop.api.TreeData;
import ht.treechop.common.chop.ChopDataImpl;
import ht.treechop.common.chop.ChopResult;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.BlockNeighbors;
import ht.treechop.common.util.FullTreeData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

public class TreeEvents {
    public static TreeData detectTree(Level level, BlockPos blockPos, Predicate<BlockPos> logCondition, int maxNumTreeBlocks) {
        if (!logCondition.test(blockPos)) {
            return new FullTreeData();
        }

        TreeData detectData = TreeChop.platform.detectTreeEvent(level, null, blockPos, level.getBlockState(blockPos), false);
        if (detectData.getLogBlocks().isPresent()) {
            return detectData;
        }

        Set<BlockPos> supportedBlocks = ChopUtil.getConnectedBlocks(
                Collections.singletonList(blockPos),
                somePos -> BlockNeighbors.HORIZONTAL_AND_ABOVE.stream(somePos)
                        .peek(pos -> detectData.setLeaves(detectData.hasLeaves() || ChopUtil.isBlockLeaves(level, pos)))
                        .filter(logCondition),
                maxNumTreeBlocks
        );

        detectData.setLogBlocks(supportedBlocks);
        return detectData;
    }

    public static boolean chopTree(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ItemStack tool, Object trigger) {
        ChopData chopData = new ChopDataImpl(
                ChopUtil.getNumChopsByTool(tool, blockState),
                ChopUtil.playerWantsToFell(agent)
        );

        boolean doChop = TreeChop.platform.startChopEvent(agent, level, pos, blockState, chopData, trigger);
        if (!doChop) {
            return true;
        }

        ChopResult chopResult = ChopUtil.getChopResult(
                level,
                pos,
                agent,
                chopData.getNumChops(),
                chopData.getFelling(),
                logPos -> ChopUtil.isBlockALog(level, logPos)
        );

        if (chopResult != ChopResult.IGNORED) {
            boolean felled = chopResult.apply(pos, agent, tool, ConfigHandler.COMMON.breakLeaves.get());
            TreeChop.platform.finishChopEvent(agent, level, pos, blockState, chopData, felled);
            tool.mineBlock(level, blockState, pos, agent);

            return !felled;
        }

        return false;
    }
}
