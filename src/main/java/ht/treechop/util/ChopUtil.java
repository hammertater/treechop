package ht.treechop.util;

import ht.treechop.block.ChoppedLogBlock;
import ht.treechop.config.ConfigHandler;
import ht.treechop.init.ModBlocks;
import ht.treechop.state.properties.BlockStateProperties;
import ht.treechop.state.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ChopUtil {

    private static final ResourceLocation MUSHROOM_STEMS = new ResourceLocation("forge", "mushroom_stems");
    private static final ResourceLocation MUSHROOM_CAPS = new ResourceLocation("forge", "mushroom_caps");

    private static final int MAX_DISTANCE_TO_DESTROY_MUSHROOM_CAPS = 7;
    public static final int FELL_NOISE_INTERVAL = 16;
    public static final int MAX_NOISE_ATTEMPTS = (FELL_NOISE_INTERVAL) * 8;

    static public boolean isBlockChoppable(IWorld world, BlockPos pos, BlockState blockState) {
        return ((blockState.getBlock() instanceof ChoppedLogBlock) ||
                (isBlockALog(blockState) && !(isBlockALog(world, pos.west()) && isBlockALog(world, pos.north()) && isBlockALog(world, pos.east()) && isBlockALog(world, pos.south()))));// && Arrays.stream(BlockNeighbors.ABOVE).map(pos::add).anyMatch(pos1 -> isBlockALog(world, pos1) || isBlockLeaves(world, pos1)))));
    }

    static public boolean isBlockChoppable(IWorld world, BlockPos pos) {
        return isBlockChoppable(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockALog(BlockState blockState) {
        Set<ResourceLocation> tags = blockState.getBlock().getTags();
        return tags.contains(BlockTags.LOGS.func_230234_a_()) || tags.contains(MUSHROOM_STEMS);
    }

    static public boolean isBlockALog(IWorld world, BlockPos pos) {
        return isBlockALog(world.getBlockState(pos));
    }

    static public boolean isBlockLeaves(IWorld world, BlockPos pos) {
        return isBlockLeaves(world.getBlockState(pos));
    }

    private static boolean isBlockLeaves(BlockState blockState) {
        Set<ResourceLocation> tags = blockState.getBlock().getTags();
        return tags.contains(BlockTags.LEAVES.func_230234_a_()) || tags.contains(MUSHROOM_CAPS);
    }

    static public Set<BlockPos> getConnectedBlocksMatchingCondition(Collection<BlockPos> startingPoints, BlockPos[] searchOffsets, Predicate<? super BlockPos> matchingCondition, int maxNumBlocks, AtomicInteger iterationCounter) {
        Set<BlockPos> connectedBlocks = new HashSet<>();
        List<BlockPos> newConnectedBlocks = new LinkedList<>(startingPoints);
        iterationCounter.set(0);
        do {
            connectedBlocks.addAll(newConnectedBlocks);
            if (connectedBlocks.size() >= maxNumBlocks) {
                break;
            }

            newConnectedBlocks = newConnectedBlocks.stream()
                    .flatMap((blockPos) -> Arrays.stream(searchOffsets).map(blockPos::add)
                            .filter(pos1 -> !connectedBlocks.contains(pos1))
                            .filter(matchingCondition)
                    )
                    .limit(maxNumBlocks - connectedBlocks.size())
                    .collect(Collectors.toList());

            iterationCounter.incrementAndGet();
        } while (!newConnectedBlocks.isEmpty());

        return connectedBlocks;
    }

    static public Set<BlockPos> getConnectedBlocksMatchingCondition(Collection<BlockPos> startingPoints, BlockPos[] searchOffsets, Predicate<? super BlockPos> matchingCondition, int maxNumBlocks) {
        return getConnectedBlocksMatchingCondition(startingPoints, searchOffsets, matchingCondition, maxNumBlocks, new AtomicInteger());
    }

    static public BlockState chipBlock(IWorld world, BlockPos blockPos, int numChops, Entity agent) {
        world.destroyBlock(blockPos, true, agent);
        ChoppedLogShape shape = ChoppedLogBlock.getPlacementShape(world, blockPos);
        BlockState blockState = ModBlocks.CHOPPED_LOG.get().getDefaultState().with(BlockStateProperties.CHOP_COUNT, numChops).with(BlockStateProperties.CHOPPED_LOG_SHAPE, shape);
        world.setBlockState(blockPos, blockState, 3);
        return blockState;
    }

    public static void fellTree(IWorld world, Collection<BlockPos> treeBlocks, Entity agent) {
        // Break leaves
        if (ConfigHandler.breakLeaves) {
            AtomicInteger blockCounter = new AtomicInteger(0);
            AtomicInteger iterationCounter = new AtomicInteger();
            getConnectedBlocksMatchingCondition(
                    treeBlocks,
                    BlockNeighbors.ADJACENTS_AND_BELOW_ADJACENTS, // Below adjacents catch red mushroom caps
                    pos -> destroyLeavesOrKeepLooking(world, pos, blockCounter, iterationCounter),
                    ConfigHandler.maxNumLeavesBlocks,
                    iterationCounter
            );
        }

        // Break logs
        for (BlockPos pos : treeBlocks) {
            AtomicInteger blockCounter = new AtomicInteger(0);
            if (blockCounter.get() < MAX_NOISE_ATTEMPTS && Math.floorMod(blockCounter.getAndIncrement(), FELL_NOISE_INTERVAL) == 0) {
                world.destroyBlock(pos, true);
            } else {
                destroyBlockQuietly(world, pos);
            }
        }
    }

    public static boolean destroyLeavesOrKeepLooking(IWorld world, BlockPos pos, AtomicInteger blockCounter, AtomicInteger iterationCounter) {
        boolean connected;
        boolean destroy;
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof LeavesBlock &&
                iterationCounter.get() + 1 == blockState.get(LeavesBlock.DISTANCE)) {
            connected = true;
            destroy = !blockState.get(LeavesBlock.PERSISTENT);
        }
        else if (blockState.getBlock().getTags().contains(MUSHROOM_CAPS) && iterationCounter.get() < MAX_DISTANCE_TO_DESTROY_MUSHROOM_CAPS) {
            connected = true;
            destroy = true;
        }
        else {
            connected = false;
            destroy = false;
        }

        if (destroy) {
            if (blockCounter.get() < MAX_NOISE_ATTEMPTS && Math.floorMod(blockCounter.getAndIncrement(), FELL_NOISE_INTERVAL) == 0) {
                world.destroyBlock(pos, true);
            } else {
                destroyBlockQuietly(world, pos);
            }
        }

        return connected;
    }

    public static void destroyBlockQuietly(IWorld world, BlockPos pos) {
        Block.spawnDrops(world.getBlockState(pos), (World) world, pos);
        world.removeBlock(pos, false);
    }

    static public int numChopsToFell(int numBlocks) {
        return (int) (ConfigHandler.chopCountingAlgorithm.calculate(numBlocks) * ConfigHandler.chopCountScale);
    }
}
