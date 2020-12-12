package ht.treechop.util;

import ht.treechop.TreeChopMod;
import ht.treechop.block.ChoppedLogBlock;
import ht.treechop.block.IChoppable;
import ht.treechop.config.ConfigHandler;
import ht.treechop.init.ModBlocks;
import ht.treechop.state.properties.BlockStateProperties;
import ht.treechop.state.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopUtil {

    private static final ResourceLocation LEAVES_LIKE = new ResourceLocation("treechop", "leaves_like");
    private static final Random RANDOM = new Random();

    private static final int MAX_DISTANCE_TO_DESTROY_LEAVES_LIKES = 7;
    public static final int FELL_NOISE_INTERVAL = 16;
    public static final int MAX_NOISE_ATTEMPTS = (FELL_NOISE_INTERVAL) * 8;

    static public boolean isBlockChoppable(World world, BlockPos pos, IBlockState blockState) {
        return ((blockState.getBlock() instanceof IChoppable) ||
                (isBlockALog(world, pos, blockState) && !(isBlockALog(world, pos.west()) && isBlockALog(world, pos.north()) && isBlockALog(world, pos.east()) && isBlockALog(world, pos.south()))));// && Arrays.stream(BlockNeighbors.ABOVE).map(pos::add).anyMatch(pos1 -> isBlockALog(world, pos1) || isBlockLeaves(world, pos1)))));
    }

    static public boolean isBlockChoppable(World world, BlockPos pos) {
        return isBlockChoppable(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockALog(World world, BlockPos blockPos, IBlockState blockState) {
        final ItemStack logItem = Blocks.LOG.getPickBlock(blockState, null, world, blockPos, null);
        ItemStack blockItem = blockState.getBlock().getPickBlock(blockState, null, world, blockPos, null);
        return OreDictionary.itemMatches(blockItem, logItem, false) || blockState.getBlock() instanceof IChoppable;
    }

    static public boolean isBlockALog(World world, BlockPos pos) {
        return isBlockALog(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockLeaves(World world, BlockPos pos) {
        return isBlockLeaves(world, pos, world.getBlockState(pos));
    }

    private static boolean isBlockLeaves(World world, BlockPos blockPos, IBlockState blockState) {
        final ItemStack logItem = Blocks.LOG.getPickBlock(blockState, null, world, blockPos, null);
        ItemStack blockItem = blockState.getBlock().getPickBlock(blockState, null, world, blockPos, null);
        return OreDictionary.itemMatches(blockItem, logItem, false);
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

    static public IBlockState chipBlock(World world, BlockPos blockPos, int numChops, EntityPlayer agent, ItemStack tool) {
        ChoppedLogShape shape = ChoppedLogBlock.getPlacementShape(world, blockPos);
        Block choppedBlock = (Block) getChoppedBlock(world.getBlockState(blockPos));
        if (choppedBlock != null) {
            IBlockState choppedState = (choppedBlock).getDefaultState()
                    .withProperty(BlockStateProperties.CHOP_COUNT, numChops)
                    .withProperty(BlockStateProperties.CHOPPED_LOG_SHAPE, shape);
            return harvestAndChangeBlock(world, blockPos, choppedState, agent, tool);
        } else {
            return null;
        }
    }

    /**
     * @return the new block state, or {@code null} if unable to break the block
     */
    private static IBlockState harvestAndChangeBlock(World world, BlockPos blockPos, IBlockState newBlockState, EntityPlayer agent, ItemStack tool) {
        if ((!tool.isEmpty() && tool.getItem().onBlockStartBreak(tool, blockPos, agent))) {
            IBlockState oldBlockState = world.getBlockState(blockPos);
            if (!agent.isCreative() && agent.canHarvestBlock(oldBlockState)) {
                TileEntity tileEntity = world.getTileEntity(blockPos);
                oldBlockState.getBlock().harvestBlock(world, agent, blockPos, oldBlockState, tileEntity, tool);
            }
            world.setBlockState(blockPos, newBlockState, 3);
            return newBlockState;
        } else {
            return null;
        }
    }

    public static void fellTree(World world, Collection<BlockPos> treeBlocks, EntityPlayer agent) {
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
                        return ((block.getTags().contains(LEAVES_LIKE) && !(block instanceof LeavesBlock))
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

    public static void destroyBlocksWithoutTooMuchNoise(World world, Collection<BlockPos> blocks, boolean spawnDrops) {
        int blockCounter = 0;
        for (BlockPos pos : blocks) {
            if (blockCounter < MAX_NOISE_ATTEMPTS && Math.floorMod(blockCounter++, FELL_NOISE_INTERVAL) == 0) {
                world.destroyBlock(pos, spawnDrops);
            } else {
                destroyBlockQuietly(world, pos, spawnDrops);
            }
        }
    }

    public static boolean markLeavesToDestroyAndKeepLooking(World world, BlockPos pos, AtomicInteger iterationCounter, Set<BlockPos> leavesToDestroy) {
        IBlockState blockState = world.getBlockState(pos);
        if (isBlockLeaves(blockState)) {
            if (blockState.getBlock() instanceof LeavesBlock) {
                if (iterationCounter.get() + 1 > blockState.get(LeavesBlock.DISTANCE)) {
                    return false;
                }
                else if (blockState.get(LeavesBlock.PERSISTENT)) {
                    return true;
                }
            } else if (blockState.getBlock().getTags().contains(LEAVES_LIKE) && iterationCounter.get() >= MAX_DISTANCE_TO_DESTROY_LEAVES_LIKES) {
                return false;
            }

            leavesToDestroy.add(pos);
            return true;
        }
        return false;
    }

    public static void destroyBlockQuietly(World world, BlockPos pos, boolean drop) {
        if (drop) {
            Block.spawnDrops(world.getBlockState(pos), (World) world, pos);
        }
        world.removeBlock(pos, false);
    }

    static public int numChopsToFell(int numBlocks) {
        return (int) (ConfigHandler.COMMON.chopCountingAlgorithm.get().calculate(numBlocks) * ConfigHandler.COMMON.chopCountScale.get());
    }

    public static ChopResult chop(World world, BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool, boolean fellIfPossible) {
        return fellIfPossible
                ? chop(world, blockPos, agent, numChops, tool)
                : chopNoFell(world, blockPos, agent, numChops, tool);
    }

    public static ChopResult chop(World world, final BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool) {
        IBlockState blockState = world.getBlockState(blockPos);
        if (!isBlockChoppable(world, blockPos, blockState)) {
            return ChopResult.IGNORED;
        }

        int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();
        Set<BlockPos> supportedBlocks = getConnectedBlocks(
                Collections.singletonList(blockPos),
                somePos -> BlockNeighbors.HORIZONTAL_AND_ABOVE.asStream(somePos)
                        .filter(checkPos -> isBlockALog(world, checkPos)),
                maxNumTreeBlocks
        );

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
                        .sorted(Comparator.comparingInt(BlockPos::getY))
                        .collect(Collectors.toList());

                int chops = 0;
                for (BlockPos pos : choppedLogsSortedByY) {
                    chops = getNumChops(world, pos);
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
                                IBlockState blockState1 = world.getBlockState(blockPos1);
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
                        IBlockState choppedState = world.getBlockState(choppedPos);
                        if (choppedState.getBlock() instanceof IChoppable) {
                            IChoppable choppedBlock = (IChoppable) choppedState.getBlock();
                            newNumChops = choppedBlock.getNumChops(choppedState) + numChops;
                            world.setBlockState(choppedPos, choppedBlock.withChops(choppedState, newNumChops), 3);
                        } else {
                            IBlockState chippedState = chipBlock(world, choppedPos, numChops, agent, tool);
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

    private static IBlockState setNumChops(World world, BlockPos blockPos, int newNumChops, EntityPlayer agent, ItemStack tool) {
        IBlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof IChoppable) {
            if (newNumChops <= ((IChoppable) block).getMaxNumChops()) {
                IBlockState newBlockState = ((IChoppable) block).withChops(blockState, newNumChops);
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

    private static int getMaxNumChops(IBlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof IChoppable ? ((IChoppable) block).getMaxNumChops() : getChoppedBlock(blockState).getMaxNumChops();
    }

    private static IChoppable getChoppedBlock(IBlockState blockState) {
        if (isBlockALog(blockState)) {
            // TODO: look up appropriate chopped block type
            return (IChoppable) (blockState.getBlock() instanceof IChoppable ? blockState.getBlock() : ModBlocks.CHOPPED_LOG.get());
        } else {
            return null;
        }
    }

    private static IBlockState getChoppedState(IBlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof IChoppable ? blockState : ((Block) getChoppedBlock(blockState)).getDefaultState();
    }

    public static int getNumChops(World world, BlockPos pos) {
        return getNumChops(world.getBlockState(pos));
    }

    public static int getNumChops(IBlockState blockState) {
        Block block = blockState.getBlock();
        return block instanceof IChoppable ? ((IChoppable) block).getNumChops(blockState) : 0;
    }

    private static ChopResult chopNoFell(World world, BlockPos blockPos, EntityPlayer agent, int numChops, ItemStack tool) {
        IBlockState blockState = world.getBlockState(blockPos);
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
            IBlockState chippedState = chipBlock(world, blockPos, numChops, agent, tool);
            return chippedState == null ? ChopResult.IGNORED : new ChopResult(blockPos, chippedState);
        }
    }

    public static int chopDistance(BlockPos a, BlockPos b) {
        return a.manhattanDistance(b);
    }
}
