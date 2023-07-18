package ht.treechop.common.chop;

import ht.treechop.TreeChop;
import ht.treechop.api.*;
import ht.treechop.common.config.ChopCounting;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.*;
import ht.treechop.server.Server;
import ht.tuber.graph.DirectedGraph;
import ht.tuber.graph.GraphUtil;
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
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopUtil {

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

    public static Stream<BlockPos> getConnectedBlocks(Collection<BlockPos> startingPoints, DirectedGraph<BlockPos> world, int maxNumBlocks, AtomicInteger iterationCounter) {
        return GraphUtil.flood(world, startingPoints, null)
                .fill();
    }

    public static List<BlockPos> getTreeLeaves(Level level, Collection<BlockPos> treeBlocks) {
        AtomicInteger iterationCounter = new AtomicInteger();
        Set<BlockPos> leaves = new HashSet<>();
        int maxDistance = ConfigHandler.COMMON.maxBreakLeavesDistance.get();

        int maxNumLeavesBlocks = ConfigHandler.COMMON.maxNumLeavesBlocks.get();
        getConnectedBlocks(
                treeBlocks,
                pos1 -> {
                    BlockState blockState = level.getBlockState(pos1);
                    return ((isBlockLeaves(blockState) && !(blockState.getBlock() instanceof LeavesBlock))
                            ? BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS // Red mushroom caps can be connected diagonally downward
                            : BlockNeighbors.ADJACENTS)
                            .asStream(pos1)
                            .filter(pos2 -> markLeavesToDestroyAndKeepLooking(level, pos2, iterationCounter, leaves, maxDistance));
                },
                maxNumLeavesBlocks,
                iterationCounter
        );

        if (leaves.size() >= maxNumLeavesBlocks) {
            TreeChop.LOGGER.warn(String.format("Max number of leaves reached: %d >= %d blocks", leaves.size(), maxNumLeavesBlocks));
        }

        return new ArrayList<>(leaves);
    }

    private static boolean markLeavesToDestroyAndKeepLooking(Level level, BlockPos pos, AtomicInteger iterationCounter, Set<BlockPos> leavesToDestroy, int maxDistance) {
        BlockState blockState = level.getBlockState(pos);
        if (isBlockLeaves(blockState)) {
            if (blockState.getBlock() instanceof LeavesBlock) {
                if (iterationCounter.get() + 1 > blockState.getValue(LeavesBlock.DISTANCE)) {
                    return false;
                }
            } else if (iterationCounter.get() >= maxDistance) {
                return false;
            }

            leavesToDestroy.add(pos);
            return true;
        }
        return false;
    }

    public static int numChopsToFell(int supportSize) {
        return ChopCounting.calculate(supportSize);
    }

    public static int numChopsToFell(Level level, Stream<BlockPos> logs) {
        int treeSize = Math.max(logs.map(pos -> (level.getBlockState(pos).getBlock() instanceof IFellableBlock block)
                                ? block.getSupportFactor(level, pos, level.getBlockState(pos))
                                : 1.0)
                        .reduce(Double::sum)
                        .orElse(1.0).intValue(),
                1);

        return numChopsToFell(treeSize);
    }

    public static ChopResult getChopResult(Level level, BlockPos origin, ChopSettings chopSettings, int numChops, boolean fellIfPossible) {
        return fellIfPossible
                ? getChopResult(level, origin, chopSettings, numChops)
                : tryToChopWithoutFelling(level, origin, numChops);
    }

    private static ChopResult getChopResult(Level level, BlockPos origin, ChopSettings chopSettings, int numChops) {
        DirectedGraph<BlockPos> world = BlockNeighbors.HORIZONTAL_AND_ABOVE::asStream;

        Set<BlockPos> base = findChoppedBlocks(level, origin);
        int baseChops = base.stream().map(pos -> getNumChops(level, pos)).reduce(Integer::sum).orElse(0);

        Predicate<BlockPos> logFilter = pos -> ChopUtil.isBlockALog(level, pos);
        TreeData tree = getTree(level, origin, base, world, logFilter, pos -> isBlockLeaves(level, pos), ConfigHandler.COMMON.maxNumTreeBlocks.get(), baseChops);

        if (tree.isAProperTree(chopSettings.getTreesMustHaveLeaves())) {
            return getChopResult(level, origin, tree, base, world, logFilter, numChops);
        } else {
            return ChopResult.IGNORED;
        }
    }

    public static TreeData getTree(Level level, BlockPos blockPos, int maxNumTreeBlocks) {
        Set<BlockPos> base = findChoppedBlocks(level, blockPos);
        int baseChops = base.stream().map(pos -> getNumChops(level, pos)).reduce(Integer::sum).orElse(0);
        return getTree(level, blockPos, base, BlockNeighbors.HORIZONTAL_AND_ABOVE::asStream, pos -> ChopUtil.isBlockALog(level, pos), pos -> isBlockLeaves(level, pos), maxNumTreeBlocks, baseChops);
    }

    public static TreeData getTree(Level level, BlockPos origin, Set<BlockPos> base, DirectedGraph<BlockPos> world, Predicate<BlockPos> logFilter, Predicate<BlockPos> leavesFilter, int maxNumTreeBlocks, int numChops) {
        if (base.isEmpty()) {
            return TreeDataImpl.empty();
        } else {
            TreeData treeData = new LazyTreeData(
                    base,
                    world,
                    logFilter,
                    leavesFilter,
                    maxNumTreeBlocks,
                    numChops
            );

            return TreeChop.platform.detectTreeEvent(level, null, origin, level.getBlockState(origin), treeData);
        }
    }

    @NotNull
    private static Set<BlockPos> findChoppedBlocks(Level level, BlockPos blockPos) {
        DirectedGraph<BlockPos> adjacentWorld = BlockNeighbors.ADJACENTS_AND_DIAGONALS::asStream;
        Set<BlockPos> base = new HashSet<>();
        base.add(blockPos);

        GraphUtil.flood(
                GraphUtil.filter(adjacentWorld, pos -> getNumChops(level, pos) > 0),
                blockPos,
                Vec3i::getY
        ).fill().forEach(base::add);

        return base;
    }

    private static ChopResult getChopResult(Level level, BlockPos origin, TreeData tree, Set<BlockPos> base, DirectedGraph<BlockPos> world, Predicate<BlockPos> logFilter, int numChops) {
        if (tree.streamLogs().findFirst().isEmpty()) {
            return ChopResult.IGNORED;
        }

        if (tree.readyToFell(tree.getChops() + numChops)) {
            return new FellTreeResult(level, tree);
        } else {
            return new ChopTreeResult(level, spillChops(level, origin, base, world, logFilter, numChops));
        }
    }

    private static List<Chop> spillChops(Level level, BlockPos origin, Set<BlockPos> base, DirectedGraph<BlockPos> treeGraph, Predicate<BlockPos> logFilter, int numChops) {
        List<Chop> chops = new Stack<>();
        AtomicInteger chopsLeft = new AtomicInteger(numChops);

        if (chopsLeft.get() > 0) {
            GraphUtil.flood(GraphUtil.filter(treeGraph, logFilter), base, a -> chopDistance(origin, a) * 32 + RandomUtils.nextInt(0, 32))
                    .fill()
                    .takeWhile(pos -> {
                        chopsLeft.set(gatherChopAndGetNumChopsRemaining(level, pos, chopsLeft.get(), chops));
                        return chopsLeft.get() > 0;
                    })
                    .count();
        }

        return chops;
    }

    private static int gatherChopAndGetNumChopsRemaining(Level level, BlockPos targetPos, int numChops, List<Chop> choppedBlocks) {
        BlockState blockStateBeforeChopping = level.getBlockState(targetPos);

        if (!(blockStateBeforeChopping.getBlock() instanceof IChoppableBlock) && isBlockSurrounded(level, targetPos)) {
            return numChops;
        }

        int adjustedNumChops = adjustNumChops(level, targetPos, blockStateBeforeChopping, numChops, false);

        if (adjustedNumChops > 0) {
            choppedBlocks.add(new Chop(targetPos, adjustedNumChops));
        }

        return numChops - adjustedNumChops;
    }

    private static boolean isBlockSurrounded(Level level, BlockPos pos) {
        return Stream.of(pos.west(), pos.north(), pos.east(), pos.south())
                .allMatch(neighborPos -> isBlockALog(level, neighborPos));
    }

    public static int adjustNumChops(Level level, BlockPos blockPos, BlockState blockState, int numChops, boolean destructive) {
        IChoppableBlock choppableBlock = ClassUtil.getChoppableBlock(level, blockPos, blockState);
        if (choppableBlock != null) {
            if (destructive) {
                return numChops;
            } else {
                int currentNumChops = choppableBlock.getNumChops(level, blockPos, blockState);
                int maxNondestructiveChops = choppableBlock.getMaxNumChops(level, blockPos, blockState) - currentNumChops;
                return Math.min(maxNondestructiveChops, numChops);
            }
        } else {
            return 0;
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

    public static int chopDistance(BlockPos a, BlockPos b) {
        return a.distManhattan(b);
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
                chopData.getFelling()
        );

        if (chopResult != ChopResult.IGNORED) {
            chopResult.apply(pos, agent, tool, ConfigHandler.COMMON.breakLeaves.get());
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

}
