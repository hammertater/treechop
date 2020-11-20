package ht.treechop.util;

import ht.treechop.TreeChopMod;
import ht.treechop.block.ChoppedLogBlock;
import ht.treechop.init.ModBlocks;
import ht.treechop.state.properties.BlockStateProperties;
import ht.treechop.state.properties.ChoppedLogShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.entity.Entity;
import net.minecraft.tags.BlockTags;
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
import java.util.stream.Stream;

public class ChopUtil {

    static public final BlockPos[] HORIZONTAL_ADJACENTS = Stream.of(
            new BlockPos(-1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] VERTICAL_ADJACENTS = Stream.of(
            new BlockPos(0, -1, 0),
            new BlockPos(0, 1, 0)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] ADJACENTS = Stream.of(
            Arrays.stream(HORIZONTAL_ADJACENTS),
            Arrays.stream(VERTICAL_ADJACENTS)
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL_DIAGONALS = Stream.of(
            new BlockPos(-1, 0, -1),
            new BlockPos(-1, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL = Stream.of(
            Arrays.stream(HORIZONTAL_ADJACENTS),
            Arrays.stream(HORIZONTAL_DIAGONALS)
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] ABOVE_ADJACENTS = Stream.of(
            new BlockPos(-1, 1, 0),
            new BlockPos(0, 1, -1),
            new BlockPos(1, 1, 0),
            new BlockPos(0, 1, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] ABOVE_DIAGONALS = Stream.of(
            new BlockPos(-1, 1, -1),
            new BlockPos(-1, 1, 1),
            new BlockPos(1, 1, -1),
            new BlockPos(1, 1, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] ABOVE = Stream.of(
            Arrays.stream(ABOVE_ADJACENTS),
            Arrays.stream(ABOVE_DIAGONALS),
            Stream.of(new BlockPos(0, 1, 0))
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] BELOW_ADJACENTS = Stream.of(
            new BlockPos(-1, -1, 0),
            new BlockPos(0, -1, -1),
            new BlockPos(1, -1, 0),
            new BlockPos(0, -1, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] BELOW_DIAGONALS = Stream.of(
            new BlockPos(-1, -1, -1),
            new BlockPos(-1, -1, 1),
            new BlockPos(1, -1, -1),
            new BlockPos(1, -1, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] BELOW = Stream.of(
            Arrays.stream(BELOW_ADJACENTS),
            Arrays.stream(BELOW_DIAGONALS),
            Stream.of(new BlockPos(0, -1, 0))
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL_AND_ABOVE = Stream.of(
            Arrays.stream(HORIZONTAL),
            Arrays.stream(ABOVE)
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public final BlockPos[] ADJACENTS_AND_DIAGONALS = Stream.of(
            Arrays.stream(ABOVE),
            Arrays.stream(HORIZONTAL),
            Arrays.stream(BELOW)
    ).flatMap(a -> a).toArray(BlockPos[]::new);

    static public boolean isBlockChoppable(IWorld world, BlockPos pos, BlockState blockState) {
        return ((!isBlockALog(world, pos.west()) || !isBlockALog(world, pos.north()) || !isBlockALog(world, pos.east()) || !isBlockALog(world, pos.south())) &&
                ((blockState.getBlock() instanceof ChoppedLogBlock) || (isBlockALog(blockState) && isBlockALog(world.getBlockState(pos.up())))));
    }

    static public boolean isBlockChoppable(IWorld world, BlockPos pos) {
        return isBlockChoppable(world, pos, world.getBlockState(pos));
    }

    static public boolean isBlockALog(BlockState blockState) {
        return blockState.getBlock().getTags().contains(BlockTags.LOGS.func_230234_a_());
    }

    static public boolean isBlockALog(IWorld world, BlockPos pos) {
        return isBlockALog(world.getBlockState(pos));
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
        final int FELL_NOISE_INTERVAL = 16;
        final int MAX_NOISE_ATTEMPTS = (FELL_NOISE_INTERVAL) * 8;

        // Break leaves
        if (TreeChopMod.breakLeaves) {
            AtomicInteger blockCounter = new AtomicInteger(0);
            AtomicInteger iterationCounter = new AtomicInteger();
            getConnectedBlocksMatchingCondition(
                    treeBlocks,
                    ADJACENTS,
                    pos -> {
                        BlockState blockState = world.getBlockState(pos);
                        if (blockState.getBlock() instanceof LeavesBlock &&
                                iterationCounter.get() + 1 == blockState.get(LeavesBlock.DISTANCE)) {
                            if (!blockState.get(LeavesBlock.PERSISTENT)) {
                                if (blockCounter.get() < MAX_NOISE_ATTEMPTS && Math.floorMod(blockCounter.getAndIncrement(), FELL_NOISE_INTERVAL) == 0) {
                                    world.destroyBlock(pos, true);
                                } else {
                                    destroyBlockQuietly(world, pos);
                                }
                            }
                            return true;
                        }
                        return false;
                    },
                    1024,
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

    public static void destroyBlockQuietly(IWorld world, BlockPos pos) {
        Block.spawnDrops(world.getBlockState(pos), (World) world, pos);
        world.removeBlock(pos, false);
    }

    static public int numChopsToFell(int numBlocks) {
        return (int) Math.floor(1 + 6 * log2(1 + ((double) numBlocks - 1) / 8));
    }

    static public double log2(double x) {
        final double invBase = 1 / (Math.log(2));
        return Math.log(x) * invBase;
    }
}
