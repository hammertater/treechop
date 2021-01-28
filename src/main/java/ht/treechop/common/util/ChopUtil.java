package ht.treechop.common.util;

import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.block.IChoppable;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.capabilities.ChopSettingsCapability;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.properties.BlockStateProperties;
import ht.treechop.common.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopUtil {

    public static boolean isBlockChoppable(IWorld world, BlockPos pos, BlockState blockState) {
        return (blockState.getBlock() instanceof IChoppable) ||
                (isBlockALog(blockState) && !(isBlockALog(world, pos.west()) && isBlockALog(world, pos.north()) && isBlockALog(world, pos.east()) && isBlockALog(world, pos.south())));
    }

    public static boolean isBlockChoppable(IWorld world, BlockPos pos) {
        return isBlockChoppable(world, pos, world.getBlockState(pos));
    }

    public static boolean isBlockALog(BlockState blockState) {
        return blockState.isIn(ConfigHandler.blockTagForDetectingLogs);
    }

    public static boolean isBlockALog(IWorld world, BlockPos pos) {
        return isBlockALog(world.getBlockState(pos));
    }

    public static boolean isBlockLeaves(IWorld world, BlockPos pos) {
        return isBlockLeaves(world.getBlockState(pos));
    }

    public static boolean isBlockLeaves(BlockState blockState) {
        if (blockState.hasProperty(LeavesBlock.PERSISTENT) && !ConfigHandler.breakPersistentLeaves && blockState.get(LeavesBlock.PERSISTENT)) {
            return false;
        } else {
            return blockState.getBlock().isIn(ConfigHandler.blockTagForDetectingLeaves);
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

    @SuppressWarnings("ConstantConditions")
    public static boolean canChangeBlock(World world, BlockPos blockPos, PlayerEntity agent) {
        return !agent.blockActionRestricted(world, blockPos, agent.getServer().getGameType());
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean canChangeBlock(World world, BlockPos blockPos, PlayerEntity agent, ItemStack tool) {
        return !(agent.blockActionRestricted(world, blockPos, agent.getServer().getGameType()) || (!tool.isEmpty() && tool.getItem().onBlockStartBreak(tool, blockPos, agent)));
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

    private static boolean markLeavesToDestroyAndKeepLooking(IWorld world, BlockPos pos, AtomicInteger iterationCounter, Set<BlockPos> leavesToDestroy) {
        BlockState blockState = world.getBlockState(pos);
        if (isBlockLeaves(blockState)) {
            if (blockState.getBlock() instanceof LeavesBlock) {
                if (iterationCounter.get() + 1 > blockState.get(LeavesBlock.DISTANCE)) {
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
        return (int) (ConfigHandler.COMMON.chopCountingAlgorithm.get().calculate(numBlocks) * ConfigHandler.COMMON.chopCountScale.get());
    }

    public static ChopResult getChopResult(World world, BlockPos blockPos, PlayerEntity agent, int numChops, boolean fellIfPossible, Predicate<BlockPos> logCondition) {
        return fellIfPossible
                ? getChopResult(world, blockPos, agent, numChops, logCondition)
                : tryToChopWithoutFelling(world, blockPos, numChops);
    }

    private static ChopResult getChopResult(World world, BlockPos blockPos, PlayerEntity agent, int numChops, Predicate<BlockPos> logCondition) {
        int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();

        AtomicBoolean hasLeaves = new AtomicBoolean(!getPlayerChopSettings(agent).getTreesMustHaveLeaves());
        Set<BlockPos> supportedBlocks = getConnectedBlocks(
                Collections.singletonList(blockPos),
                somePos -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(somePos)
                        .peek(pos -> hasLeaves.compareAndSet(false, isBlockLeaves(world, pos)))
                        .filter(logCondition),
                maxNumTreeBlocks
        );

        if (!hasLeaves.get()) {
            return ChopResult.IGNORED;
        }

        if (supportedBlocks.size() >= maxNumTreeBlocks) {
            TreeChopMod.LOGGER.warn(String.format("Max tree size reached: %d >= %d blocks (not including leaves)", supportedBlocks.size(), maxNumTreeBlocks));
        }

        return chopTree(world, blockPos, supportedBlocks, numChops);
    }

    private static ChopResult chopTree(World world, BlockPos target, Set<BlockPos> supportedBlocks, int numChops) {
        BlockState blockState = world.getBlockState(target);
        int currentNumChops = getNumChops(blockState);
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
                        .filter(pos1 -> world.getBlockState(pos1).getBlock() instanceof IChoppable)
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
                return gatherChops(world, target, numChops, nearbyChoppableBlocks);
            }
        }

        supportedBlocks.remove(target);
        return new ChopResult(world, Collections.singletonList(target), supportedBlocks);
    }

    /**
     * Adds chops to the targeted block without destroying it. Overflow chops spill to nearby blocks.
     * @param nearbyChoppableBlocks must not include {@code target}
     */
    private static ChopResult gatherChops(World world, BlockPos target, int numChops, Set<BlockPos> nearbyChoppableBlocks) {
        List<TreeBlock> choppedBlocks = new LinkedList<>();
        int numChopsLeft = gatherChopAndGetNumChopsRemaining(world, target, numChops, choppedBlocks);

        if (numChopsLeft > 0) {
            List<BlockPos> sortedChoppableBlocks = nearbyChoppableBlocks.stream()
                    .filter(blockPos1 -> {
                        BlockState blockState1 = world.getBlockState(blockPos1);
                        Block block1 = blockState1.getBlock();
                        if (block1 instanceof IChoppable) {
                            return getNumChops(blockState1) < getMaxNumChops(world, blockPos1, blockState1);
                        } else {
                            return blockPos1.getY() >= target.getY();
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
                            numChopsLeft = gatherChopAndGetNumChopsRemaining(world, nextTarget, numChopsLeft, choppedBlocks);
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

        return new ChopResult(choppedBlocks);
    }

    private static int gatherChopAndGetNumChopsRemaining(World world, BlockPos target, int numChops, List<TreeBlock> choppedBlocks) {
        BlockState blockStateBeforeChopping = world.getBlockState(target);
        BlockState blockStateAfterChopping = getBlockStateAfterChops(world, target, numChops, false);

        if (blockStateBeforeChopping != blockStateAfterChopping) {
            choppedBlocks.add(new TreeBlock(world, target, blockStateAfterChopping, true));
        }

        return numChops - (getNumChops(blockStateAfterChopping) - getNumChops(blockStateBeforeChopping));
    }

    public static BlockState getBlockStateAfterChops(World world, BlockPos blockPos, int numChops, boolean destructive) {
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof IChoppable) {
            return getBlockStateAfterChops((IChoppable) block, blockState, numChops, destructive);
        } else {
            if (isBlockChoppable(world, blockPos, blockState)) {
                IChoppable choppedBlock = getChoppedBlock(blockState);
                if (choppedBlock instanceof Block) {
                    ChoppedLogShape shape = ChoppedLogBlock.getPlacementShape(world, blockPos);
                    BlockState defaultChoppedState = ((Block) choppedBlock).getDefaultState().with(BlockStateProperties.CHOPPED_LOG_SHAPE, shape);
                    return getBlockStateAfterChops(
                            choppedBlock,
                            defaultChoppedState,
                            numChops - getNumChops(defaultChoppedState),
                            destructive
                    );
                } else {
                    throw new IllegalArgumentException(String.format("Block \"%s\" is not choppable", block.getRegistryName()));
                }
            } else {
                return blockState;
            }
        }
    }

    public static BlockState getBlockStateAfterChops(IChoppable choppableBlock, BlockState blockState, int numChops, boolean destructive) {
        int currentNumChops = getNumChops(blockState);
        int maxNumChops = choppableBlock.getMaxNumChops();
        int newNumChops = currentNumChops + numChops;

        if (newNumChops <= maxNumChops) {
            return choppableBlock.withChops(blockState, newNumChops);
        } else {
            return (destructive)
                    ? Blocks.AIR.getDefaultState()
                    : choppableBlock.withChops(blockState, maxNumChops);
        }
    }

    public static int getMaxNumChops(World world, BlockPos blockPos, BlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof IChoppable) {
            return ((IChoppable) block).getMaxNumChops();
        } else {
            if (isBlockChoppable(world, blockPos, world.getBlockState(blockPos))) {
                IChoppable choppedBlock = getChoppedBlock(blockState);
                return (choppedBlock != null) ? choppedBlock.getMaxNumChops() : 0;
            } else {
                return 0;
            }
        }
    }

    public static IChoppable getChoppedBlock(BlockState blockState) {
        if (isBlockALog(blockState)) {
            return (IChoppable) (blockState.getBlock() instanceof IChoppable ? blockState.getBlock() : ModBlocks.CHOPPED_LOG.get());
        } else {
            return null;
        }
    }

    public static int getNumChops(World world, BlockPos pos) {
        return getNumChops(world.getBlockState(pos));
    }

    public static int getNumChops(BlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof IChoppable ? ((IChoppable) block).getNumChops(blockState) : 0;
    }

    public static int getNumChops(World world, Set<BlockPos> positions) {
        return positions.stream()
                .map(world::getBlockState)
                .map(blockState1 -> blockState1.getBlock() instanceof IChoppable
                        ? ((IChoppable) blockState1.getBlock()).getNumChops(blockState1)
                        : 0
                )
                .reduce(Integer::sum)
                .orElse(0);
    }

    private static ChopResult tryToChopWithoutFelling(World world, BlockPos blockPos, int numChops) {
        return (isBlockChoppable(world, blockPos))
                ? new ChopResult(Collections.singletonList(
                        new TreeBlock(world, blockPos, getBlockStateAfterChops(world, blockPos, numChops, true), true)
                ), false)
                : ChopResult.IGNORED;
    }

    public static int chopDistance(BlockPos a, BlockPos b) {
        return a.manhattanDistance(b);
    }

    public static boolean canChopWithTool(ItemStack tool) {
        return !(ConfigHandler.choppingToolItemsBlacklist.contains(tool.getItem().getRegistryName()) ||
                tool.getItem().getTags().stream().anyMatch(ConfigHandler.choppingToolTagsBlacklist::contains));
    }

    public static int getNumChopsByTool(ItemStack tool) {
        return 1;
    }

    public static boolean playerWantsToChop(PlayerEntity player) {
        if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
            ChopSettings chopSettings = getPlayerChopSettings(player);
            if (!player.isCreative() || chopSettings.getChopInCreativeMode()) {
                return chopSettings.getChoppingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
            } else {
                return chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
            }
        } else {
            return true;
        }
    }

    public static boolean playerWantsToFell(PlayerEntity player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
            return chopSettings.getFellingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeFellBehavior(player);
        } else {
            return true;
        }
    }

    private static boolean isLocalPlayer(PlayerEntity player) {
        return !player.isServerWorld() && Minecraft.getInstance().player == player;
    }

    @SuppressWarnings("ConstantConditions")
    public static ChopSettings getPlayerChopSettings(PlayerEntity player) {
        return isLocalPlayer(player) ? Client.getChopSettings() : ChopSettingsCapability.forPlayer(player).orElse(new ChopSettingsCapability());
    }

    public static void doItemDamage(ItemStack itemStack, World world, BlockState blockState, BlockPos blockPos, PlayerEntity agent) {
        ItemStack mockItemStack = itemStack.copy();
        itemStack.onBlockDestroyed(world, blockState, blockPos, agent);
        if (itemStack.isEmpty() && !mockItemStack.isEmpty()) {
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(agent, mockItemStack, Hand.MAIN_HAND);
        }
    }

    public static void dropExperience(World world, BlockPos blockPos, int amount) {
        if (world instanceof ServerWorld) {
            Blocks.AIR.dropXpOnBlockBreak((ServerWorld) world, blockPos, amount);
        }
    }

}
