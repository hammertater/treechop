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
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopUtil {

    private static final Random RANDOM = new Random();

    private static final int MAX_DISTANCE_TO_DESTROY_LEAVES_LIKES = 7;
    public static final int FELL_NOISE_INTERVAL = 16;
    public static final int MAX_NOISE_ATTEMPTS = (FELL_NOISE_INTERVAL) * 8;

    static public boolean isBlockChoppable(IWorld world, BlockPos pos, Block block) {
        return (block instanceof IChoppable) ||
                (isBlockALog(block) && !(isBlockALog(world, pos.west()) && isBlockALog(world, pos.north()) && isBlockALog(world, pos.east()) && isBlockALog(world, pos.south())));
    }

    static public boolean isBlockChoppable(IWorld world, BlockPos pos) {
        return isBlockChoppable(world, pos, world.getBlockState(pos).getBlock());
    }

    static public boolean isBlockALog(Block block) {
        return block.getTags().contains(ConfigHandler.blockTagForDetectingLogs);
    }

    static public boolean isBlockALog(IWorld world, BlockPos pos) {
        return isBlockALog(world.getBlockState(pos).getBlock());
    }

    static public boolean isBlockLeaves(IWorld world, BlockPos pos) {
        return isBlockLeaves(world.getBlockState(pos).getBlock());
    }

    private static boolean isBlockLeaves(Block block) {
        return block.getBlock().getTags().contains(ConfigHandler.blockTagForDetectingLeaves);
    }

    static public Set<BlockPos> getConnectedBlocks(Collection<BlockPos> startingPoints, Function<BlockPos, Stream<BlockPos>> searchOffsetsSupplier, int maxNumBlocks, AtomicInteger iterationCounter) {
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

    static public Set<BlockPos> getConnectedBlocks(Collection<BlockPos> startingPoints, Function<BlockPos, Stream<BlockPos>> searchOffsetsSupplier, int maxNumBlocks) {
        return getConnectedBlocks(startingPoints, searchOffsetsSupplier, maxNumBlocks, new AtomicInteger());
    }

    /**
     * If the block at {@code blockPos} is chippable, harvest it and replace with a chopped block.
     * @return the new {@code BlockState} after chipping or {@code null} if the existing block cannot be chipped
     */
    static public BlockState chipBlock(World world, BlockPos blockPos, int numChops, PlayerEntity agent, ItemStack tool) {
        ChoppedLogShape shape = ChoppedLogBlock.getPlacementShape(world, blockPos);
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof IChoppable) {
            throw new IllegalArgumentException("Block is already chipped");
        }

        Block choppedBlock = (Block) getChoppedBlock(blockState);
        if (choppedBlock != null) {
            BlockState choppedState = choppedBlock.getDefaultState()
                    .with(BlockStateProperties.CHOP_COUNT, numChops)
                    .with(BlockStateProperties.CHOPPED_LOG_SHAPE, shape);
            return harvestAndChangeBlock(world, blockPos, choppedState, agent, tool);
        } else {
            return null;
        }
    }

    /**
     * @return the new block state, or {@code null} if unable to break the block
     */
    @SuppressWarnings("ConstantConditions")
    private static BlockState harvestAndChangeBlock(World world, BlockPos blockPos, BlockState newBlockState, PlayerEntity agent, ItemStack tool) {
        if (!agent.blockActionRestricted(world, blockPos, agent.getServer().getGameType()) && !tool.onBlockStartBreak(blockPos, agent)) {
            BlockState oldBlockState = world.getBlockState(blockPos);
            if (!agent.isCreative() && oldBlockState.canHarvestBlock(world, blockPos, agent)) {
                TileEntity tileEntity = world.getTileEntity(blockPos);
                Block.spawnDrops(oldBlockState, world, blockPos, tileEntity, agent, tool);
            }
            world.setBlockState(blockPos, newBlockState, 3);
            return newBlockState;
        } else {
            return null;
        }
    }

    public static void fellTree(IWorld world, Collection<BlockPos> treeBlocks, PlayerEntity agent) {
        boolean spawnDrops = !agent.isCreative();

        // Break leaves
        if (ConfigHandler.COMMON.breakLeaves.get()) {
            AtomicInteger iterationCounter = new AtomicInteger();
            Set<BlockPos> leavesToDestroy = new HashSet<>();

            int maxNumLeavesBlocks = ConfigHandler.COMMON.maxNumLeavesBlocks.get();
            getConnectedBlocks(
                    treeBlocks,
                    pos1 -> {
                        Block block = world.getBlockState(pos1).getBlock();
                        return ((isBlockLeaves(block) && !(block instanceof LeavesBlock))
                                    ? BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS // Red mushroom caps can be connected diagonally downward
                                    : BlockNeighbors.ADJACENTS)
                            .asStream(pos1)
                            .filter(pos2 -> markLeavesToDestroyAndKeepLooking(world, pos2, iterationCounter, leavesToDestroy));
                    },
                    maxNumLeavesBlocks,
                    iterationCounter
            );

            if (leavesToDestroy.size() >= maxNumLeavesBlocks) {
                TreeChopMod.LOGGER.warn(String.format("Max number of leaves reached: %d >= %d blocks", leavesToDestroy.size(), maxNumLeavesBlocks));
            }

            destroyBlocksWithoutTooMuchNoise(world, leavesToDestroy, spawnDrops);
        }

        destroyBlocksWithoutTooMuchNoise(world, treeBlocks, spawnDrops);
    }

    public static void destroyBlocksWithoutTooMuchNoise(IWorld world, Collection<BlockPos> blocks, boolean spawnDrops) {
        int blockCounter = 0;
        for (BlockPos pos : blocks) {
            if (blockCounter < MAX_NOISE_ATTEMPTS && Math.floorMod(blockCounter++, FELL_NOISE_INTERVAL) == 0) {
                world.destroyBlock(pos, spawnDrops);
            } else {
                destroyBlockQuietly(world, pos, spawnDrops);
            }
        }
    }

    public static boolean markLeavesToDestroyAndKeepLooking(IWorld world, BlockPos pos, AtomicInteger iterationCounter, Set<BlockPos> leavesToDestroy) {
        BlockState blockState = world.getBlockState(pos);
        if (isBlockLeaves(blockState.getBlock())) {
            if (blockState.getBlock() instanceof LeavesBlock) {
                if (iterationCounter.get() + 1 > blockState.get(LeavesBlock.DISTANCE)) {
                    return false;
                }
                else if (blockState.get(LeavesBlock.PERSISTENT)) {
                    return true;
                }
            } else if (iterationCounter.get() >= MAX_DISTANCE_TO_DESTROY_LEAVES_LIKES) {
                return false;
            }

            leavesToDestroy.add(pos);
            return true;
        }
        return false;
    }

    public static void destroyBlockQuietly(IWorld world, BlockPos pos, boolean drop) {
        if (drop) {
            Block.spawnDrops(world.getBlockState(pos), (World) world, pos);
        }
        world.removeBlock(pos, false);
    }

    static public int numChopsToFell(int numBlocks) {
        return (int) (ConfigHandler.COMMON.chopCountingAlgorithm.get().calculate(numBlocks) * ConfigHandler.COMMON.chopCountScale.get());
    }

    public static ChopResult chop(World world, BlockPos blockPos, PlayerEntity agent, int numChops, ItemStack tool, boolean fellIfPossible) {
        return fellIfPossible
                ? chop(world, blockPos, agent, numChops, tool)
                : chopNoFell(world, blockPos, agent, numChops, tool);
    }

    public static ChopResult chop(World world, final BlockPos blockPos, PlayerEntity agent, int numChops, ItemStack tool) {
        BlockState blockState = world.getBlockState(blockPos);
        if (!isBlockChoppable(world, blockPos, blockState.getBlock())) {
            return ChopResult.IGNORED;
        }

        int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();

        AtomicBoolean hasLeaves = new AtomicBoolean(!getPlayerChopSettings(agent).getOnlyChopTreesWithLeaves());
        Set<BlockPos> supportedBlocks = getConnectedBlocks(
                Collections.singletonList(blockPos),
                somePos -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(somePos)
                        .peek(pos -> hasLeaves.compareAndSet(false, isBlockLeaves(world, pos)))
                        .filter(checkPos -> isBlockALog(world, checkPos)),
                maxNumTreeBlocks
        );

        if (!hasLeaves.get()) {
            return ChopResult.IGNORED;
        }

        if (supportedBlocks.size() >= maxNumTreeBlocks) {
            TreeChopMod.LOGGER.warn(String.format("Max tree size reached: %d >= %d blocks (not including leaves)", supportedBlocks.size(), maxNumTreeBlocks));
        }

        int numChopsToFell = numChopsToFell(supportedBlocks.size());
        int currentNumChops = getNumChops(blockState);

        if (currentNumChops + numChops >= numChopsToFell) {
            fellTree(world, supportedBlocks, agent);
            return new ChopResult(blockPos, blockState);
        } else {
            Set<BlockPos> nearbyChoppableBlocks;
            nearbyChoppableBlocks = ChopUtil.getConnectedBlocks(
                    Collections.singletonList(blockPos),
                    pos -> BlockNeighbors.ADJACENTS_AND_DIAGONALS.asStream(pos)
                            .filter(checkPos -> Math.abs(checkPos.getY() - blockPos.getY()) < 4 && isBlockChoppable(world, checkPos)),
                    64
            );

            int totalNumChops = nearbyChoppableBlocks.stream()
                    .map(world::getBlockState)
                    .map(blockState1 -> blockState1.getBlock() instanceof IChoppable
                            ? ((IChoppable) blockState1.getBlock()).getNumChops(blockState1)
                            : 0
                    )
                    .reduce(Integer::sum)
                    .orElse(0)
                    + numChops; // Include this chop

            if (totalNumChops >= numChopsToFell) {
                List<BlockPos> choppedLogsSortedByY = nearbyChoppableBlocks.stream()
                        .filter(pos1 -> world.getBlockState(pos1).getBlock() instanceof IChoppable)
                        .sorted(Comparator.comparingInt(Vector3i::getY))
                        .collect(Collectors.toList());

                for (BlockPos pos : choppedLogsSortedByY) {
                    int chops = getNumChops(world, pos);
                    supportedBlocks.add(pos);
                    if (chops > numChopsToFell) {
                        break;
                    }
                }

                fellTree(world, supportedBlocks, agent);
                return new ChopResult(blockPos, blockState);
            } else {
                int newNumChops;
                if ((newNumChops = currentNumChops + numChops) <= getMaxNumChops(blockState)) {
                    setNumChops(world, blockPos, newNumChops, agent, tool);
                    return new ChopResult(blockPos, blockState);
                } else { // If this block is out of chops, chop another block
                    int chopsToTargetedBlock = getMaxNumChops(blockState) - currentNumChops;
                    if (chopsToTargetedBlock > 0) {
                        setNumChops(world, blockPos, getMaxNumChops(blockState), agent, tool);
                        numChops -= chopsToTargetedBlock;
                    }

                    List<BlockPos> sortedChoppableBlocks = nearbyChoppableBlocks.stream()
                            .filter(blockPos1 -> {
                                BlockState blockState1 = world.getBlockState(blockPos1);
                                Block block1 = blockState1.getBlock();
                                if (block1 instanceof IChoppable) {
                                    return ((IChoppable) block1).getNumChops(blockState1) < 7;
                                } else {
                                    return blockPos1.getY() >= blockPos.getY();
                                }
                            })
                            .sorted(Comparator.comparingInt(a -> chopDistance(blockPos, a)))
                            .collect(Collectors.toList());

                    if (!sortedChoppableBlocks.isEmpty()) {
                        // Find a close, choppable block...
                        int choiceIndexLimit = 1;
                        for (int maxChoiceDistance = chopDistance(blockPos, sortedChoppableBlocks.get(0)), n = sortedChoppableBlocks.size(); choiceIndexLimit < n; ++choiceIndexLimit) {
                            if (chopDistance(blockPos, sortedChoppableBlocks.get(choiceIndexLimit)) > maxChoiceDistance) {
                                break;
                            }
                        }

                        // ...and chop it
                        BlockPos choppedPos = sortedChoppableBlocks.get(Math.floorMod(RANDOM.nextInt(), choiceIndexLimit));
                        BlockState choppedState = world.getBlockState(choppedPos);
                        if (choppedState.getBlock() instanceof IChoppable) {
                            IChoppable choppedBlock = (IChoppable) choppedState.getBlock();
                            newNumChops = choppedBlock.getNumChops(choppedState) + numChops;
                            world.setBlockState(choppedPos, choppedBlock.withChops(choppedState, newNumChops), 3);
                        } else {
                            BlockState chippedState = chipBlock(world, choppedPos, numChops, agent, tool);
                            if (chippedState == null) {
                                return ChopResult.IGNORED;
                            }
                        }
                        return new ChopResult(choppedPos, choppedState);
                    } else {
                        return ChopResult.IGNORED;
                    }
                }
            }
        }
    }

    private static BlockState setNumChops(World world, BlockPos blockPos, int newNumChops, PlayerEntity agent, ItemStack tool) {
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof IChoppable) {
            if (newNumChops <= ((IChoppable) block).getMaxNumChops()) {
                BlockState newBlockState = ((IChoppable) block).withChops(blockState, newNumChops);
                if (world.setBlockState(blockPos, newBlockState)) {
                    return newBlockState;
                } else {
                    throw new IllegalArgumentException("Failed to chop block located at " + blockPos.toString());
                }
            } else {
                throw new IllegalArgumentException("Can't set number of chops above the maximum allowed");
            }
        } else {
            return chipBlock(world, blockPos, newNumChops, agent, tool);
        }
    }

    private static int getMaxNumChops(BlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof IChoppable) {
            return ((IChoppable) block).getMaxNumChops();
        } else {
            IChoppable choppedBlock = getChoppedBlock(blockState);
            return (choppedBlock != null) ? choppedBlock.getMaxNumChops() : 0;
        }
    }

    private static IChoppable getChoppedBlock(BlockState blockState) {
        if (isBlockALog(blockState.getBlock())) {
            // TODO: look up appropriate chopped block type
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

    private static ChopResult chopNoFell(World world, BlockPos blockPos, PlayerEntity agent, int numChops, ItemStack tool) {
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock() instanceof IChoppable) {
            IChoppable block = (IChoppable) blockState.getBlock();
            int newNumChops = block.getNumChops(blockState) + numChops;
            if (newNumChops <= block.getMaxNumChops()) {
                world.setBlockState(blockPos, block.withChops(blockState, newNumChops));
                return new ChopResult(blockPos, blockState);
            } else {
                return ChopResult.IGNORED;
            }
        } else {
            BlockState chippedState = chipBlock(world, blockPos, numChops, agent, tool);
            return chippedState == null ? ChopResult.IGNORED : new ChopResult(blockPos, chippedState);
        }
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
        ChopSettings chopSettings = getPlayerChopSettings(player);
        if (ConfigHandler.COMMON.canChooseNotToChop.get()) {
            return chopSettings.getChoppingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeChopBehavior(player);
        } else {
            return true;
        }
    }

    public static boolean playerWantsToFell(PlayerEntity player) {
        ChopSettings chopSettings = getPlayerChopSettings(player);
        return chopSettings.getFellingEnabled() ^ chopSettings.getSneakBehavior().shouldChangeFellBehavior(player);
    }

    private static boolean isLocalPlayer(PlayerEntity player) {
        return !player.isServerWorld() && Minecraft.getInstance().player == player;
    }

    @SuppressWarnings("ConstantConditions")
    private static ChopSettings getPlayerChopSettings(PlayerEntity player) {
        return isLocalPlayer(player) ? Client.getChopSettings() : player.getCapability(ChopSettingsCapability.CAPABILITY).orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty"));
    }

    public static void doExhaustion(PlayerEntity agent) {
        agent.addExhaustion(ConfigHandler.COMMON.chopExhaustionAmount.get().floatValue());
    }

    public static void doItemDamage(ItemStack itemStack, World world, BlockState blockState, BlockPos blockPos, PlayerEntity agent) {
        ItemStack mockItemStack = itemStack.copy();
        itemStack.onBlockDestroyed(world, blockState, blockPos, agent);
        if (itemStack.isEmpty() && !mockItemStack.isEmpty()) {
            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(agent, mockItemStack, Hand.MAIN_HAND);
        }
    }

    public static void dropExperience(World world, BlockPos blockPos, BlockState blockState, int amount) {
        if (world instanceof ServerWorld) {
            blockState.getBlock().dropXpOnBlockBreak((ServerWorld) world, blockPos, amount);
        }
    }
}
