package ht.treechop.common.chop;

import ht.treechop.TreeChop;
import ht.treechop.api.*;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.config.ChopCounting;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.Lazy;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.AxeAccessor;
import ht.treechop.common.util.BlockUtil;
import ht.treechop.common.util.ClassUtil;
import ht.treechop.server.Server;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class ChopUtil {

    private static final Lazy<ITreeBlock> defaultDetector = new Lazy<>(
            ConfigHandler.RELOAD,
            () -> new TreeDetectorBuilder().build()
    );

    public static boolean isBlockALog(Level level, BlockPos pos) {
        return isBlockALog(level, pos, level.getBlockState(pos));
    }

    public static boolean isBlockALog(Level level, BlockPos pos, BlockState blockState) {
        return isBlockChoppable(level, pos, blockState);
    }

    public static boolean isBlockChoppable(Level level, BlockPos pos) {
        return isBlockChoppable(level, pos, level.getBlockState(pos));
    }

    public static boolean isBlockChoppable(BlockGetter level, BlockPos pos, BlockState blockState) {
        return ClassUtil.getChoppableBlock(level, pos, blockState) != null;
    }

    public static boolean isBlockLeaves(Level level, BlockPos pos, BlockState state) {
        return isBlockLeaves(state);
    }

    public static boolean isBlockLeaves(Level level, BlockPos pos) {
        return isBlockLeaves(level.getBlockState(pos));
    }

    public static boolean isBlockLeaves(BlockState blockState) {
        if (ConfigHandler.COMMON.leavesBlocks.get().contains(blockState.getBlock())) {
            return !ConfigHandler.COMMON.ignorePersistentLeaves.get() || !blockState.hasProperty(LeavesBlock.PERSISTENT) || !blockState.getValue(LeavesBlock.PERSISTENT);
        } else {
            return false;
        }
    }

    public static boolean enoughChopsToFell(int chops, double support) {
        return ChopCounting.calculate((int) support) <= chops;
    }

    public static int numChopsToFell(Level level, Stream<BlockPos> logs) {
        int support = Math.max(
                logs.map(pos -> getSupportFactor(level, pos))
                        .reduce(Double::sum)
                        .orElse(1.0).intValue(),
                1
        );

        return ChopCounting.calculate(support);
    }

    public static Optional<Double> getSupportFactor(Level level, Stream<BlockPos> blocks) {
        return blocks.map(pos -> ChopUtil.getSupportFactor(level, pos)).reduce(Double::sum);
    }

    public static double getSupportFactor(Level level, BlockPos pos) {
        return getSupportFactor(level, pos, level.getBlockState(pos));
    }

    private static double getSupportFactor(Level level, BlockPos pos, BlockState state) {
        return (level.getBlockState(pos).getBlock() instanceof IFellableBlock block)
                ? block.getSupportFactor(level, pos, state)
                : 1.0;
    }

    public static ChopResult getChopResult(Level level, BlockPos origin, ChopSettings chopSettings, int numChops, boolean fellIfPossible, boolean breakLeaves) {
        return fellIfPossible
                ? getChopResult(level, origin, chopSettings, numChops, breakLeaves)
                : tryToChopWithoutFelling(level, origin, numChops);
    }

    private static ChopResult getChopResult(Level level, BlockPos origin, ChopSettings chopSettings, int numChops, boolean breakLeaves) {
        TreeData tree = getTree(level, origin);

        if (tree.isAProperTree(chopSettings.getTreesMustHaveLeaves())) {
            return getChopResult(level, origin, tree, numChops, breakLeaves);
        } else {
            return ChopResult.IGNORED;
        }
    }

    public static TreeData getTree(Level level, BlockPos origin) {
        ITreeBlock detector = ClassUtil.getTreeBlock(getLogBlock(level, origin));
        if (detector == null) {
            detector = defaultDetector.get();
        }

        TreeData tree = detector.getTree(level, origin);

        return TreeChop.platform.detectTreeEvent(level, null, origin, level.getBlockState(origin), tree);
    }

    private static ChopResult getChopResult(Level level, BlockPos origin, TreeData tree, int numChops, boolean breakLeaves) {
        if (tree.streamLogs().findFirst().isEmpty()) {
            return ChopResult.IGNORED;
        }

        if (tree.readyToFell(tree.getChops() + numChops)) {
            return new FellTreeResult(level, tree, breakLeaves);
        } else {
            return new ChopTreeResult(level, tree.chop(origin, numChops));
        }
    }

    public static int getMaxNumChops(Level level, BlockPos blockPos, BlockState blockState) {
        IChoppableBlock choppableBlock = ClassUtil.getChoppableBlock(level, blockPos, blockState);
        return (choppableBlock != null) ? choppableBlock.getMaxNumChops(level, blockPos, blockState) : 0;
    }

    public static int getNumChops(Level level, BlockPos pos) {
        return getNumChops(level, pos, level.getBlockState(pos));
    }

    public static int getNumChops(Level level, BlockPos pos, BlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof IChoppableBlock choppableBlock ? choppableBlock.getNumChops(level, pos, blockState) : 0;
    }

    public static int getNumChops(Level level, Set<BlockPos> positions) {
        return positions.stream()
                .map(pos -> Pair.of(pos, level.getBlockState(pos)))
                .map(posAndblockState -> posAndblockState.getRight().getBlock() instanceof IChoppableBlock choppableBlock
                        ? choppableBlock.getNumChops(level, posAndblockState.getLeft(), posAndblockState.getRight())
                        : 0
                )
                .reduce(Integer::sum)
                .orElse(0);
    }

    private static ChopResult tryToChopWithoutFelling(Level level, BlockPos blockPos, int numChops) {
        return (isBlockChoppable(level, blockPos))
                ? new ChopTreeResult(level, Collections.singletonList(new Chop(blockPos, numChops)))
                : ChopResult.IGNORED;
    }

    public static int blockDistance(BlockPos a, BlockPos b) {
        return a.distManhattan(b);
    }

    public static int horizontalBlockDistance(BlockPos a, BlockPos b) {
        return new Vec3i(a.getX(), 0, a.getZ()).distManhattan(new Vec3i(b.getX(), 0, b.getZ()));
    }

    public static boolean canChopWithTool(Player player, Level level, BlockPos pos) {
        return canChopWithTool(player, player.getMainHandItem(), level, pos, level.getBlockState(pos));

    }

    public static boolean canChopWithTool(Player player, ItemStack tool, Level level, BlockPos pos, BlockState blockState) {
        return (!ConfigHandler.COMMON.mustUseCorrectToolForDrops.get() || !blockState.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(blockState))
                && (!ConfigHandler.COMMON.mustUseFastBreakingTool.get() || tool.getDestroySpeed(blockState) > 1f)
                && ConfigHandler.canChopWithTool(player, tool, level, pos, blockState);
    }

    public static int getNumChopsByTool(ItemStack tool, BlockState blockState) {
        IChoppingItem choppingItem = ClassUtil.getChoppingItem(tool.getItem());
        if (choppingItem != null) {
            return choppingItem.getNumChops(tool, blockState);
        } else {
            return 1;
        }
    }

    public static boolean playerWantsToChop(Player player, ChopSettings chopSettings) {
        if (ConfigHandler.COMMON.enabled.get() && (player != null && !player.isCreative() || chopSettings.getChopInCreativeMode())) {
            return chopSettings.getChoppingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
        } else {
            return false;
        }
    }

    public static boolean playerWantsToFell(Player player, ChopSettings chopSettings) {
        return chopSettings.getFellingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeFellBehavior(player);
    }

    public static boolean chop(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ItemStack tool, Object trigger) {
        ChopSettings chopSettings = Server.instance().getPlayerChopData(agent).getSettings();
        if (!isBlockChoppable(level, pos, blockState)
                || !ChopUtil.playerWantsToChop(agent, chopSettings)
                || !ChopUtil.canChopWithTool(agent, tool, level, pos, blockState)) {
            return false;
        }

        ChopData chopData = new ChopDataImpl(
                ChopUtil.getNumChopsByTool(tool, blockState),
                ChopUtil.playerWantsToFell(agent, chopSettings)
        );

        boolean doChop = TreeChop.platform.startChopEvent(agent, level, pos, blockState, chopData, trigger);
        if (!doChop) {
            return false;
        }

        ChopResult chopResult = ChopUtil.getChopResult(
                level,
                pos,
                chopSettings,
                chopData.getNumChops(),
                chopData.getFelling(),
                ConfigHandler.COMMON.breakLeaves.get()
        );

        if (chopResult != ChopResult.IGNORED) {
            chopResult.apply(pos, agent, tool);
            TreeChop.platform.finishChopEvent(agent, level, pos, blockState, chopData, chopResult);
            tool.mineBlock(level, blockState, pos, agent);

            boolean felled = chopResult instanceof FellTreeResult;
            return !felled;
        }

        return false;
    }

    public static BlockState getStrippedState(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return getStrippedState(level, pos, state, state);
    }

    /**
     * Only use for visual purposes, does not affect gameplay
     */
    public static BlockState getStrippedState(BlockAndTintGetter level, BlockPos pos, BlockState state, BlockState fallback) {
        BlockState strippedState = (AxeAccessor.isStripped(state.getBlock())) ? state : AxeAccessor.getStripped(state);
        if (strippedState == null) {
            strippedState = TreeChop.platform.getStrippedState(level, pos, state);
            if (strippedState == null) {
                IStrippableBlock strippableBlock = ClassUtil.getStrippableBlock(state.getBlock());
                if (strippableBlock != null) {
                    return strippableBlock.getStrippedState(level, pos, state);
                } else {
                    strippedState = ConfigHandler.inferredStrippedStates.get().get(state.getBlock());
                }
            }
        }

        return (strippedState != null) ? BlockUtil.copyStateProperties(strippedState, state) : fallback;
    }

    public static BlockState getLogState(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            return entity.getOriginalState();
        } else {
            return level.getBlockState(pos);
        }
    }

    public static Block getLogBlock(Level level, BlockPos pos) {
        return getLogBlock(level, pos, level.getBlockState(pos));
    }

    public static Block getLogBlock(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            return entity.getOriginalState().getBlock();
        } else {
            return state.getBlock();
        }
    }

}
