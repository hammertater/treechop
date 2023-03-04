package ht.treechop.common.chop;

import ht.treechop.TreeChop;
import ht.treechop.api.*;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.platform.Platform;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.EntityChopSettings;
import ht.treechop.common.util.*;
import ht.treechop.server.Server;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

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

    public static Set<BlockPos> getConnectedBlocks(Collection<BlockPos> startingPoints, Function<BlockPos, Stream<BlockPos>> searchOffsetsSupplier, int maxNumBlocks, AtomicInteger iterationCounter) {
        Set<BlockPos> connectedBlocks = new HashSet<>();
        List<BlockPos> newConnectedBlocks = new LinkedList<>(startingPoints);
        iterationCounter.set(0);
        do {
            connectedBlocks.addAll(newConnectedBlocks);
            if (connectedBlocks.size() >= maxNumBlocks) {
                break;
            }

            newConnectedBlocks = newConnectedBlocks.stream()
                    .flatMap(blockPos -> searchOffsetsSupplier.apply(blockPos)
                            .filter(pos1 -> !connectedBlocks.contains(pos1))
                    )
                    .limit(maxNumBlocks - connectedBlocks.size())
                    .collect(Collectors.toList());

            iterationCounter.incrementAndGet();
        } while (!newConnectedBlocks.isEmpty());

        return connectedBlocks;
    }

    public static Set<BlockPos> getConnectedBlocks(Collection<BlockPos> startingPoints, Function<BlockPos, Stream<BlockPos>> searchOffsetsSupplier, int maxNumBlocks) {
        return getConnectedBlocks(startingPoints, searchOffsetsSupplier, maxNumBlocks, new AtomicInteger());
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
        return ConfigHandler.COMMON.chopCountingAlgorithm.get().calculate(supportSize);
    }

    public static int numChopsToFell(Level level, Set<BlockPos> supportedBlocks) {
        int treeSize = Math.max(supportedBlocks.stream()
                        .map(pos -> (level.getBlockState(pos).getBlock() instanceof IFellableBlock block)
                                ? block.getSupportFactor(level, pos, level.getBlockState(pos))
                                : 1.0)
                        .reduce(Double::sum)
                        .orElse(1.0).intValue(),
                1);

        return numChopsToFell(treeSize);
    }

    public static ChopResult getChopResult(Level level, BlockPos blockPos, Player agent, int numChops, boolean fellIfPossible, Predicate<BlockPos> logCondition) {
        return fellIfPossible
                ? getChopResult(level, blockPos, agent, numChops, logCondition)
                : tryToChopWithoutFelling(level, blockPos, numChops);
    }

    private static ChopResult getChopResult(Level level, BlockPos blockPos, Player agent, int numChops, Predicate<BlockPos> logCondition) {
        int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();
        TreeData tree = getTreeBlocks(level, blockPos, logCondition, maxNumTreeBlocks);
        if (tree.getLogBlocksOrEmpty().size() >= maxNumTreeBlocks) {
            TreeChop.LOGGER.warn("Max tree size {} reached (not including leaves)", maxNumTreeBlocks);
        }

        if (tree.isAProperTree(getPlayerChopSettings(agent).getTreesMustHaveLeaves())) {
            Set<BlockPos> supportedBlocks = tree.getLogBlocks().orElse(Collections.emptySet());
            return getChopResult(level, blockPos, supportedBlocks, numChops);
        } else {
            return ChopResult.IGNORED;
        }
    }

    public static TreeData getTreeBlocks(Level level, BlockPos blockPos, int maxNumTreeBlocks) {
        return getTreeBlocks(level, blockPos, pos -> ChopUtil.isBlockALog(level, pos), maxNumTreeBlocks);
    }

    public static TreeData getTreeBlocks(Level level, BlockPos blockPos, Predicate<BlockPos> logCondition, int maxNumTreeBlocks) {
        if (!logCondition.test(blockPos)) {
            return new TreeDataImpl();
        }

        TreeData detectData = TreeChop.platform.detectTreeEvent(level, null, blockPos, level.getBlockState(blockPos), false);
        if (detectData.getLogBlocks().isPresent()) {
            return detectData;
        }

        Set<BlockPos> supportedBlocks = getConnectedBlocks(
                Collections.singletonList(blockPos),
                somePos -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(somePos)
                        .peek(pos -> detectData.setLeaves(detectData.hasLeaves() || isBlockLeaves(level, pos)))
                        .filter(logCondition),
                maxNumTreeBlocks
        );

        detectData.setLogBlocks(supportedBlocks);
        return detectData;
    }

    private static ChopResult getChopResult(Level level, BlockPos target, Set<BlockPos> supportedBlocks, int numChops) {
        if (supportedBlocks.isEmpty()) {
            return ChopResult.IGNORED;
        }

        BlockState blockState = level.getBlockState(target);
        int currentNumChops = getNumChops(level, target, blockState);
        int numChopsToFell = numChopsToFell(level, supportedBlocks);

        if (currentNumChops + numChops < numChopsToFell) {
            Set<BlockPos> nearbyChoppableBlocks;
            nearbyChoppableBlocks = ChopUtil.getConnectedBlocks(
                    Collections.singletonList(target),
                    pos -> BlockNeighbors.ADJACENTS_AND_DIAGONALS.asStream(pos)
                            .filter(checkPos -> Math.abs(checkPos.getY() - target.getY()) < 4 && isBlockChoppable(level, checkPos)),
                    64
            );

            int totalNumChops = getNumChops(level, nearbyChoppableBlocks) + numChops;

            if (totalNumChops >= numChopsToFell) {
                List<BlockPos> choppedLogsSortedByY = nearbyChoppableBlocks.stream()
                        .filter(pos1 -> level.getBlockState(pos1).getBlock() instanceof IChoppableBlock)
                        .sorted(Comparator.comparingInt(Vec3i::getY))
                        .collect(Collectors.toList());

                // Consume nearby chopped blocks that contributed even if they're at a lower Y, but prefer higher ones
                for (BlockPos pos : choppedLogsSortedByY) {
                    int chops = getNumChops(level, pos);
                    supportedBlocks.add(pos);
                    if (chops > numChopsToFell) {
                        break;
                    }
                }
            } else {
                nearbyChoppableBlocks.remove(target);
                return gatherChops(level, target, numChops, nearbyChoppableBlocks);
            }
        }

        Chop chop = new Chop(target, numChops);
        return new ChopResult(level, Collections.singletonList(chop), supportedBlocks);
    }

    /**
     * Adds chops to the targeted block without destroying it. Overflow chops spill to nearby blocks.
     *
     * @param nearbyChoppableBlocks must not include {@code target}
     */
    private static ChopResult gatherChops(Level level, BlockPos target, int numChops, Set<BlockPos> nearbyChoppableBlocks) {
        List<Chop> chops = new Stack<>();
        int numChopsLeft = gatherChopAndGetNumChopsRemaining(level, target, numChops, chops);

        if (numChopsLeft > 0) {
            List<BlockPos> sortedChoppableBlocks = nearbyChoppableBlocks.stream()
                    .filter(pos -> {
                        BlockState blockState = level.getBlockState(pos);
                        if (blockState.getBlock() instanceof IChoppableBlock) {
                            return getNumChops(level, pos, blockState) < getMaxNumChops(level, pos, blockState);
                        } else {
                            return pos.getY() >= target.getY();
                        }
                    })
                    .sorted(Comparator.comparingInt(a -> chopDistance(target, a)))
                    .collect(Collectors.toList());

            if (sortedChoppableBlocks.size() > 0) {
                int nextChoiceDistance = chopDistance(target, sortedChoppableBlocks.get(0));
                int candidateStartIndex = 0;
                for (int i = 0, n = sortedChoppableBlocks.size(); i <= n; ++i) {
                    if (i == n || chopDistance(target, sortedChoppableBlocks.get(i)) > nextChoiceDistance) {
                        List<BlockPos> candidates = sortedChoppableBlocks.subList(candidateStartIndex, i);
                        Collections.shuffle(candidates);

                        for (BlockPos nextTarget : candidates) {
                            numChopsLeft = gatherChopAndGetNumChopsRemaining(level, nextTarget, numChopsLeft, chops);
                            if (numChopsLeft <= 0) {
                                break;
                            }
                        }

                        if (numChopsLeft <= 0) {
                            break;
                        }
                        candidateStartIndex = i;
                    }
                }
            }
        }

        return new ChopResult(level, chops, Collections.emptyList());
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
                ? new ChopResult(level, Collections.singletonList(new Chop(blockPos, numChops)), Collections.emptyList())
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

    public static boolean playerWantsToChop(Player player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        return playerWantsToChop(player, chopSettings);
    }

    public static boolean playerWantsToChop(Player player, ChopSettings chopSettings) {
        if (ConfigHandler.COMMON.enabled.get() && (player != null && !player.isCreative() || chopSettings.getChopInCreativeMode())) {
            return chopSettings.getChoppingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
        } else {
            return false;
        }
    }

    public static boolean playerWantsToFell(Player player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        return playerWantsToFell(player, chopSettings);
    }

    public static boolean playerWantsToFell(Player player, ChopSettings chopSettings) {
        return chopSettings.getFellingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeFellBehavior(player);
    }

    public static EntityChopSettings getPlayerChopSettings(Player player) {
        return Server.instance().getPlayerChopSettings(player);
    }

    public static void dropExperience(Level level, BlockPos pos, int amount) {
        if (level instanceof ServerLevel serverLevel && level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS)) {
            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), amount);
        }
    }

    public static boolean chop(ServerPlayer agent, ServerLevel level, BlockPos pos, BlockState blockState, ItemStack tool, Object trigger) {
        if (!isBlockChoppable(level, pos, blockState)
                || !ChopUtil.playerWantsToChop(agent)
                || !ChopUtil.canChopWithTool(agent, tool, level, pos, blockState)) {
            return false;
        }

        ChopData chopData = new ChopDataImpl(
                ChopUtil.getNumChopsByTool(tool, blockState),
                ChopUtil.playerWantsToFell(agent)
        );

        boolean doChop = TreeChop.platform.startChopEvent(agent, level, pos, blockState, chopData, trigger);
        if (!doChop) {
            return false;
        }

        ChopResult chopResult = ChopUtil.getChopResult(
                level,
                pos,
                agent,
                chopData.getNumChops(),
                chopData.getFelling(),
                logPos -> isBlockALog(level, logPos)
        );

        if (chopResult != ChopResult.IGNORED) {
            boolean felled = chopResult.apply(pos, agent, tool, ConfigHandler.COMMON.breakLeaves.get());
            TreeChop.platform.finishChopEvent(agent, level, pos, blockState, chopData, felled);
            tool.mineBlock(level, blockState, pos, agent);

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
                strippedState = (strippableBlock != null)
                        ? strippableBlock.getStrippedState(level, pos, state)
                        : ConfigHandler.inferredStrippedStates.get().get(state.getBlock());
                if (strippedState == null) {
                    strippedState = fallback;
                }
            }
        }

        return BlockUtil.copyStateProperties(strippedState, state);
    }

}
