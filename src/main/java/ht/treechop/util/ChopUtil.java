package ht.treechop.util;

import ht.treechop.block.ChoppedLog;
import ht.treechop.init.ModBlocks;
import ht.treechop.state.properties.BlockStateProperties;
import ht.treechop.state.properties.ChoppedLogShape;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
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
    ).flatMap(a->a).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL_DIAGONALS = Stream.of(
            new BlockPos(-1, 0, -1),
            new BlockPos(-1, 0, 1),
            new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 1)
    ).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL = Stream.of(
            Arrays.stream(HORIZONTAL_ADJACENTS),
            Arrays.stream(HORIZONTAL_DIAGONALS)
    ).flatMap(a->a).toArray(BlockPos[]::new);

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
    ).flatMap(a->a).toArray(BlockPos[]::new);

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
    ).flatMap(a->a).toArray(BlockPos[]::new);

    static public final BlockPos[] HORIZONTAL_AND_ABOVE = Stream.of(
            Arrays.stream(HORIZONTAL),
            Arrays.stream(ABOVE)
    ).flatMap(a->a).toArray(BlockPos[]::new);

    static public final BlockPos[] ADJACENTS_AND_DIAGONALS = Stream.of(
            Arrays.stream(ABOVE),
            Arrays.stream(HORIZONTAL),
            Arrays.stream(BELOW)
    ).flatMap(a->a).toArray(BlockPos[]::new);

    static public boolean isBlockChoppable(IWorld world, BlockPos pos, BlockState blockState) {
        return ((world.isAirBlock(pos.west()) || world.isAirBlock(pos.north()) || world.isAirBlock(pos.east()) || world.isAirBlock(pos.south())) &&
                ((blockState.getBlock() instanceof ChoppedLog) || (isBlockALog(blockState) && isBlockALog(world.getBlockState(pos.up())))));
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

    static public Set<BlockPos> getConnectedBlocksMatchingCondition(Collection<BlockPos> startingPoints, BlockPos[] searchOffsets, Predicate<? super BlockPos> matchingCondition, int maxNumBlocks) {
        Set<BlockPos> connectedBlocks = new HashSet<>();
        List<BlockPos> newConnectedBlocks = new LinkedList<>(startingPoints);
        do {
            connectedBlocks.addAll(newConnectedBlocks);
            if (connectedBlocks.size() >= maxNumBlocks) {
                break;
            }

            newConnectedBlocks = newConnectedBlocks.stream()
                    .flatMap((blockPos) -> Arrays.stream(searchOffsets).map(blockPos::add)
                            .filter(pos2 -> !connectedBlocks.contains(pos2))
                            .filter(matchingCondition)
                    )
                    .limit(maxNumBlocks - connectedBlocks.size())
                    .collect(Collectors.toList());
        } while (!newConnectedBlocks.isEmpty());

        return connectedBlocks;
    }

    static public BlockState chipBlock(IWorld world, BlockPos blockPos, int numChops, Entity agent) {
        world.destroyBlock(blockPos, true, agent);
        ChoppedLogShape shape = ChoppedLog.getPlacementShape(world, blockPos);
        BlockState blockState = ModBlocks.CHOPPED_LOG.get().getDefaultState().with(BlockStateProperties.CHOP_COUNT, numChops).with(BlockStateProperties.CHOPPED_LOG_SHAPE, shape);
        world.setBlockState(blockPos, blockState, 3);
        return blockState;
    }

    public static void fellTree(IWorld world, Collection<BlockPos> treeBlocks, Entity agent) {
        AtomicReference<BlockPos> aMinPos = new AtomicReference<>(new BlockPos(treeBlocks.iterator().next()));
        AtomicReference<BlockPos> aMaxPos = new AtomicReference<>(new BlockPos(aMinPos.get()));
        treeBlocks.forEach(blockPos -> {
            aMinPos.set(new BlockPos(
                    Math.min(blockPos.getX(), aMinPos.get().getX()),
                    Math.min(blockPos.getY(), aMinPos.get().getY()),
                    Math.min(blockPos.getZ(), aMinPos.get().getZ())
            ));
            aMaxPos.set(new BlockPos(
                    Math.max(blockPos.getX(), aMaxPos.get().getX()),
                    Math.max(blockPos.getY(), aMaxPos.get().getY()),
                    Math.max(blockPos.getZ(), aMaxPos.get().getZ())
            ));
        });

        BlockPos minPos = new BlockPos(aMinPos.get()).add(-7, -2, -7);
        BlockPos maxPos = new BlockPos(aMaxPos.get()).add(7, 7, 7);

        Set<BlockPos> leaves = getConnectedBlocksMatchingCondition(
                treeBlocks,
                ADJACENTS_AND_DIAGONALS,
                pos -> {
                    Set<ResourceLocation> tags = world.getBlockState(pos).getBlock().getTags();
                    return (
                            (
                                    pos.getX() >= minPos.getX() &&
                                    pos.getY() >= minPos.getY() &&
                                    pos.getZ() >= minPos.getZ() &&
                                    pos.getX() <= maxPos.getX() &&
                                    pos.getY() <= maxPos.getY() &&
                                    pos.getZ() <= maxPos.getZ()
                            ) && (
                                tags.contains(BlockTags.LEAVES.func_230234_a_()) ||
                                tags.contains(BlockTags.BEEHIVES.func_230234_a_()) ||
                                tags.contains(BlockTags.BEE_GROWABLES.func_230234_a_())
                            )
                    );
                },
                treeBlocks.size() * 2 + 64
        );

        leaves.forEach(pos -> world.destroyBlock(pos, true, agent)); // p_225521_2_ is whether to spawn drops
        treeBlocks.forEach(pos -> world.destroyBlock(pos, true, agent)); // p_225521_2_ is whether to spawn drops
    }

    static public int numChopsToFell(int numBlocks) {
        return (int) Math.floor(1 + 6 * log2(1 + ((double) numBlocks - 1) / 8));
    }

    static public double log2(double x) {
        final double invBase = 1 / (Math.log(2));
        return Math.log(x) * invBase;
    }

    static public Stream<BlockPos> getPositionsWithOffsets(Collection<BlockPos> startingPoints, BlockPos[] searchOffsets) {
        return startingPoints.stream()
                .flatMap(pos -> Arrays.stream(searchOffsets).map(pos::add));
    }

//    static public boolean allMatch(Collection<BlockPos> posList, Predicate<? super BlockPos> matchingCondition) {
//        posList.
//                .allMatch((blockPos) -> Arrays.stream(searchOffsets)
//                        .map(blockPos::add)
//                        .allMatch(matchingCondition)
//                );
//    }
}
