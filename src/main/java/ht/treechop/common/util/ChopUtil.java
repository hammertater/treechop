package ht.treechop.common.util;

import ht.treechop.TreeChopMod;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.api.IChoppingItem;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.event.ChopEvent;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.settings.ChopSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopUtil {

    public static boolean isBlockChoppable(World world, BlockPos pos, BlockState blockState) {
        return (blockState.getBlock() instanceof IChoppableBlock) || (isBlockALog(blockState));
    }

    public static boolean isBlockChoppable(World world, BlockPos pos) {
        return isBlockChoppable(world, pos, world.getBlockState(pos));
    }

    public static boolean isBlockALog(BlockState blockState) {
        return blockState.is(ConfigHandler.blockTagForDetectingLogs);
    }

    public static boolean isBlockALog(World world, BlockPos pos) {
        return isBlockALog(world.getBlockState(pos));
    }

    public static boolean isBlockLeaves(World world, BlockPos pos) {
        return isBlockLeaves(world.getBlockState(pos));
    }

    public static boolean isBlockLeaves(BlockState blockState) {
        if (blockState.is(ConfigHandler.blockTagForDetectingLeaves)) {
            return !ConfigHandler.ignorePersistentLeaves || !blockState.hasProperty(LeavesBlock.PERSISTENT) || !blockState.getValue(LeavesBlock.PERSISTENT);
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

    public static boolean canChangeBlock(World world, BlockPos blockPos, PlayerEntity agent, GameType gameType) {
        return canChangeBlock(world, blockPos, agent, gameType, ItemStack.EMPTY);
    }

    public static boolean canChangeBlock(World world, BlockPos blockPos, PlayerEntity agent, GameType gameType, ItemStack tool) {
        if (!agent.blockActionRestricted(world, blockPos, gameType)) { // TODO: get the player's game mode
            if (tool.isEmpty()) {
                return true;
            } else {
                return ConfigHandler.shouldOverrideItemBehavior(tool.getItem(), true) || !tool.getItem().onBlockStartBreak(tool, blockPos, agent);
            }
        }
        else {
            return false;
        }
    }

    public static List<BlockPos> getTreeLeaves(World world, Collection<BlockPos> treeBlocks) {
        AtomicInteger iterationCounter = new AtomicInteger();
        Set<BlockPos> leaves = new HashSet<>();

        int maxNumLeavesBlocks = ConfigHandler.COMMON.maxNumLeavesBlocks.get();
        getConnectedBlocks(
                treeBlocks,
                pos1 -> {
                    BlockState blockState = world.getBlockState(pos1);
                    return ((isBlockLeaves(blockState) && !(blockState.getBlock() instanceof LeavesBlock))
                            ? BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS // Red mushroom caps can be connected diagonally downward
                            : BlockNeighbors.ADJACENTS)
                            .asStream(pos1)
                            .filter(pos2 -> markLeavesToDestroyAndKeepLooking(world, pos2, iterationCounter, leaves));
                },
                maxNumLeavesBlocks,
                iterationCounter
        );

        if (leaves.size() >= maxNumLeavesBlocks) {
            TreeChopMod.LOGGER.warn(String.format("Max number of leaves reached: %d >= %d blocks", leaves.size(), maxNumLeavesBlocks));
        }

        return new ArrayList<>(leaves);
    }

    private static boolean markLeavesToDestroyAndKeepLooking(World world, BlockPos pos, AtomicInteger iterationCounter, Set<BlockPos> leavesToDestroy) {
        BlockState blockState = world.getBlockState(pos);
        if (isBlockLeaves(blockState)) {
            if (blockState.getBlock() instanceof LeavesBlock) {
                if (iterationCounter.get() + 1 > blockState.getValue(LeavesBlock.DISTANCE)) {
                    return false;
                }
            } else if (iterationCounter.get() >= ConfigHandler.maxBreakLeavesDistance) {
                return false;
            }

            leavesToDestroy.add(pos);
            return true;
        }
        return false;
    }

    public static int numChopsToFell(int numBlocks) {
        return ConfigHandler.COMMON.chopCountingAlgorithm.get().calculate(numBlocks);
    }

    public static ChopResult getChopResult(World world, BlockPos blockPos, PlayerEntity agent, int numChops, boolean fellIfPossible, Predicate<BlockPos> logCondition) {
        return fellIfPossible
                ? getChopResult(world, blockPos, agent, numChops, logCondition)
                : tryToChopWithoutFelling(world, blockPos, numChops);
    }

    private static ChopResult getChopResult(World world, BlockPos blockPos, PlayerEntity agent, int numChops, Predicate<BlockPos> logCondition) {
        Set<BlockPos> supportedBlocks = getTreeBlocks(world, blockPos, logCondition, getPlayerChopSettings(agent).getTreesMustHaveLeaves());
        return chopTree(world, blockPos, supportedBlocks, numChops);
    }

    private static Set<BlockPos> getTreeBlocks(World world, BlockPos blockPos, Predicate<BlockPos> logCondition, boolean mustHaveLeaves) {
        AtomicBoolean hasLeaves = new AtomicBoolean(!mustHaveLeaves);
        Set<BlockPos> treeBlocks = getTreeBlocks(world, blockPos, logCondition, hasLeaves);
        return hasLeaves.get() ? treeBlocks : Collections.emptySet();
    }

    private static Set<BlockPos> getTreeBlocks(World world, BlockPos blockPos, Predicate<BlockPos> logCondition, AtomicBoolean inHasLeaves) {
        if (!logCondition.test(blockPos)) {
            return Collections.emptySet();
        }

        AtomicBoolean overrideHasLeaves = new AtomicBoolean(inHasLeaves.get());
        ChopEvent.DetectTreeEvent detectEvent = new ChopEvent.DetectTreeEvent(world, null, blockPos, world.getBlockState(blockPos), inHasLeaves, overrideHasLeaves);
        boolean valueToOverrideHasLeaves = inHasLeaves.get();

        boolean canceled = MinecraftForge.EVENT_BUS.post(detectEvent);
        if (canceled) {
            return Collections.emptySet();
        }

        int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();

        AtomicBoolean trueHasLeaves = new AtomicBoolean(false);
        Set<BlockPos> supportedBlocks = getConnectedBlocks(
                Collections.singletonList(blockPos),
                somePos -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(somePos)
                        .peek(pos -> trueHasLeaves.compareAndSet(false, isBlockLeaves(world, pos)))
                        .filter(logCondition),
                maxNumTreeBlocks
        );

        if (supportedBlocks.size() >= maxNumTreeBlocks) {
            TreeChopMod.LOGGER.warn(String.format("Max tree size reached: %d >= %d blocks (not including leaves)", supportedBlocks.size(), maxNumTreeBlocks));
        }

        inHasLeaves.set(overrideHasLeaves.get() ? valueToOverrideHasLeaves : trueHasLeaves.get());

        return supportedBlocks;
    }

    private static ChopResult chopTree(World world, BlockPos target, Set<BlockPos> supportedBlocks, int numChops) {
        if (supportedBlocks.isEmpty()) {
            return ChopResult.IGNORED;
        }

        BlockState blockState = world.getBlockState(target);
        int currentNumChops = getNumChops(world, target, blockState);
        int numChopsToFell = numChopsToFell(supportedBlocks.size());

        if (currentNumChops + numChops < numChopsToFell) {
            Set<BlockPos> nearbyChoppableBlocks;
            nearbyChoppableBlocks = ChopUtil.getConnectedBlocks(
                    Collections.singletonList(target),
                    pos -> BlockNeighbors.ADJACENTS_AND_DIAGONALS.asStream(pos)
                            .filter(checkPos -> Math.abs(checkPos.getY() - target.getY()) < 4 && isBlockChoppable(world, checkPos)),
                    64
            );

            int totalNumChops = getNumChops(world, nearbyChoppableBlocks) + numChops;

            if (totalNumChops >= numChopsToFell) {
                List<BlockPos> choppedLogsSortedByY = nearbyChoppableBlocks.stream()
                        .filter(pos1 -> world.getBlockState(pos1).getBlock() instanceof IChoppableBlock)
                        .sorted(Comparator.comparingInt(Vector3i::getY))
                        .collect(Collectors.toList());

                // Consume nearby chopped blocks that contributed even if they're at a lower Y, but prefer higher ones
                for (BlockPos pos : choppedLogsSortedByY) {
                    int chops = getNumChops(world, pos);
                    supportedBlocks.add(pos);
                    if (chops > numChopsToFell) {
                        break;
                    }
                }
            } else {
                nearbyChoppableBlocks.remove(target);
                return gatherChops(world, target, numChops, nearbyChoppableBlocks);
            }
        }

        Chop chop = new Chop(target, numChops);
        return new ChopResult(world, Collections.singletonList(chop), supportedBlocks);
    }

    /**
     * Adds chops to the targeted block without destroying it. Overflow chops spill to nearby blocks.
     * @param nearbyChoppableBlocks must not include {@code target}
     */
    private static ChopResult gatherChops(World world, BlockPos target, int numChops, Set<BlockPos> nearbyChoppableBlocks) {
        List<Chop> chops = new Stack<>();
        int numChopsLeft = gatherChopAndGetNumChopsRemaining(world, target, numChops, chops);

        if (numChopsLeft > 0) {
            List<BlockPos> sortedChoppableBlocks = nearbyChoppableBlocks.stream()
                    .filter(pos -> {
                        BlockState blockState = world.getBlockState(pos);
                        if (blockState.getBlock() instanceof IChoppableBlock) {
                            return getNumChops(world, pos, blockState) < getMaxNumChops(world, pos, blockState);
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
                            numChopsLeft = gatherChopAndGetNumChopsRemaining(world, nextTarget, numChopsLeft, chops);
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

        return new ChopResult(world, chops, Collections.emptyList());
    }

    private static int gatherChopAndGetNumChopsRemaining(World world, BlockPos targetPos, int numChops, List<Chop> choppedBlocks) {
        BlockState blockStateBeforeChopping = world.getBlockState(targetPos);

        if (!(blockStateBeforeChopping.getBlock() instanceof IChoppableBlock) && isBlockSurrounded(world, targetPos)) {
            return numChops;
        }

        int adjustedNumChops = adjustNumChops(world, targetPos, blockStateBeforeChopping, numChops, false);

        if (adjustedNumChops > 0) {
            choppedBlocks.add(new Chop(targetPos, adjustedNumChops));
        }

        return numChops - adjustedNumChops;
    }

    private static boolean isBlockSurrounded(World world, BlockPos pos) {
        return Stream.of(pos.west(), pos.north(), pos.east(), pos.south())
                .allMatch(neighborPos -> isBlockALog(world, neighborPos));
    }

    public static int adjustNumChops(World world, BlockPos blockPos, BlockState blockState, int numChops, boolean destructive) {
        Block choppedBlock = getChoppedBlock(blockState);
        if (choppedBlock instanceof IChoppableBlock) {
            if (destructive) {
                return numChops;
            } else {
                int currentNumChops = (blockState.is(choppedBlock)) ? ((IChoppableBlock) choppedBlock).getNumChops(world, blockPos, blockState) : 0;
                int maxNondestructiveChops = ((IChoppableBlock) choppedBlock).getMaxNumChops(world, blockPos, blockState) - currentNumChops;
                return Math.min(maxNondestructiveChops, numChops);
            }
        }
        return 0;
    }

    public static int getMaxNumChops(World world, BlockPos blockPos, BlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof IChoppableBlock) {
            return ((IChoppableBlock) block).getMaxNumChops(world, blockPos, blockState);
        } else {
            if (isBlockChoppable(world, blockPos, world.getBlockState(blockPos))) {
                Block choppedBlock = getChoppedBlock(blockState);
                return (choppedBlock instanceof IChoppableBlock) ? ((IChoppableBlock) choppedBlock).getMaxNumChops(world, blockPos, blockState) : 0;
            } else {
                return 0;
            }
        }
    }

    public static Block getChoppedBlock(BlockState blockState) {
        if (isBlockALog(blockState)) {
            return blockState.getBlock() instanceof IChoppableBlock ? blockState.getBlock() : ModBlocks.CHOPPED_LOG.get();
        } else {
            return null;
        }
    }

    public static int getNumChops(World world, BlockPos pos) {
        return getNumChops(world, pos, world.getBlockState(pos));
    }

    public static int getNumChops(World world, BlockPos pos, BlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof IChoppableBlock ? ((IChoppableBlock) block).getNumChops(world, pos, blockState) : 0;
    }

    public static int getNumChops(World world, Set<BlockPos> positions) {
        return positions.stream()
                .map(pos -> Pair.of(pos, world.getBlockState(pos)))
                .map(posAndblockState -> {
                    Block block = posAndblockState.getRight().getBlock();
                    return posAndblockState.getRight().getBlock() instanceof IChoppableBlock
                            ? ((IChoppableBlock) block).getNumChops(world, posAndblockState.getLeft(), posAndblockState.getRight())
                            : 0;
                })
                .reduce(Integer::sum)
                .orElse(0);
    }

    private static ChopResult tryToChopWithoutFelling(World world, BlockPos blockPos, int numChops) {
        return (isBlockChoppable(world, blockPos))
                ? new ChopResult(world, Collections.singletonList(new Chop(blockPos, numChops)), Collections.emptyList())
                : ChopResult.IGNORED;
    }

    public static int chopDistance(BlockPos a, BlockPos b) {
        return a.distManhattan(b);
    }

    public static boolean canChopWithTool(ItemStack tool) {
        return ConfigHandler.canChopWithItem(tool.getItem());
    }

    public static int getNumChopsByTool(ItemStack tool, BlockState blockState) {
        Item toolItem = tool.getItem();

        Integer overrideChops = ConfigHandler.getNumChopsOverride(tool.getItem());
        if (overrideChops != null) {
            return overrideChops;
        } else if (toolItem instanceof IChoppingItem) {
            return ((IChoppingItem) toolItem).getNumChops(tool, blockState);
        } else {
            return 1;
        }
    }

    public static boolean playerWantsToChop(PlayerEntity player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        return playerWantsToChop(player, chopSettings);
    }

    public static boolean playerWantsToChop(PlayerEntity player, ChopSettings chopSettings) {
        if (!player.isCreative() || chopSettings.getChopInCreativeMode()) {
            return chopSettings.getChoppingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
        } else {
            return false;
        }
    }

    public static boolean playerWantsToFell(PlayerEntity player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        return playerWantsToFell(player, chopSettings);
    }

    public static boolean playerWantsToFell(PlayerEntity player, ChopSettings chopSettings) {
        return chopSettings.getFellingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeFellBehavior(player);
    }

    public static ChopSettings getPlayerChopSettings(PlayerEntity player) {
        LazyOptional<ChopSettings> playerSettings = ChopSettingsCapability.forPlayer(player).cast();
        return playerSettings.orElse(ConfigHandler.fakePlayerChopSettings);
    }

    public static void doItemDamage(ItemStack itemStack, World world, BlockState blockState, BlockPos blockPos, PlayerEntity agent) {
        ItemStack mockItemStack = itemStack.copy();
        itemStack.mineBlock(world, blockState, blockPos, agent);
        if (itemStack.isEmpty() && !mockItemStack.isEmpty()) {
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(agent, mockItemStack, Hand.MAIN_HAND);
        }
    }

    public static void dropExperience(World world, BlockPos blockPos, int amount) {
        if (world instanceof ServerWorld) {
            Blocks.AIR.popExperience((ServerWorld) world, blockPos, amount);
        }
    }

    public static boolean isPartOfATree(World world, BlockPos pos, boolean mustHaveLeaves) {
        AtomicBoolean hasLeaves = new AtomicBoolean(false);
        Set<BlockPos> treeBlocks = getTreeBlocks(world, pos, blockPos -> isBlockALog(world, blockPos), hasLeaves);

        if (treeBlocks.isEmpty()) {
            return false;
        } else {
            if (mustHaveLeaves) {
                return hasLeaves.get();
            } else {
                return treeBlocks.size() >= (hasLeaves.get() ? 1 : 2);
            }
        }
    }
}
