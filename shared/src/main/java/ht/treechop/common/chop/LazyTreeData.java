package ht.treechop.common.chop;

import ht.treechop.api.AbstractTreeData;
import ht.treechop.api.IChoppableBlock;
import ht.treechop.common.util.BlockNeighbors;
import ht.treechop.common.util.ClassUtil;
import ht.tuber.graph.DirectedGraph;
import ht.tuber.graph.FloodFill;
import ht.tuber.graph.FloodFillImpl;
import ht.tuber.graph.GraphUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.RandomUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LazyTreeData extends AbstractTreeData {

    private final Level level;
    private final int chops;
    private final boolean smartDetection;
    private final int maxLeavesDistance;
    private double mass = 0;
    private boolean overrideLeaves = false;

    private Set<BlockPos> logs = new HashSet<>() {
        @Override
        public boolean add(BlockPos blockPos) {
            if (super.add(blockPos)) {
                mass += ChopUtil.getSupportFactor(level, blockPos);
                return true;
            }
            return false;
        }
    };

    private final Set<BlockPos> leaves = new HashSet<>() {
        @Override
        public boolean add(BlockPos blockPos) {
            return super.add(blockPos);
        }
    };

    private LogFinder logFinder;
    private Set<BlockPos> base;

    private final DirectedGraph<BlockPos> logsWorld;
    private final DirectedGraph<BlockPos> leavesWorld;

    public LazyTreeData(Level level, BlockPos origin, DirectedGraph<BlockPos> logGraph, DirectedGraph<BlockPos> leavesGraph, Predicate<BlockPos> logFilter, Predicate<BlockPos> leavesFilter, int maxNumLogs, int maxLeavesDistance, boolean smartDetection) {
        this.level = level;
        this.smartDetection = smartDetection;
        this.maxLeavesDistance = maxLeavesDistance;

        logsWorld = GraphUtil.filter(
                logGraph,
                this::gatherLog,
                pos -> check(pos, logFilter, leavesFilter)
        );

        leavesWorld = GraphUtil.filterNeighbors(
                leavesGraph,
                leavesFilter
        );

        makeTreeBase(level, origin);
        logs.addAll(base);
        this.chops = base.stream().map(pos -> ChopUtil.getNumChops(level, pos)).reduce(Integer::sum).orElse(0);

        logFinder = new LogFinder(logsWorld, base, maxNumLogs);
    }

    private boolean gatherLog(BlockPos pos) {
        logs.add(pos);
        return true;
    }

    private boolean check(BlockPos pos, Predicate<BlockPos> logFilter, Predicate<BlockPos> leavesFilter) {
        if (leavesFilter.test(pos)) {
            leaves.add(pos);
        }
        return logFilter.test(pos);
    }

    @Override
    public boolean hasLeaves() {
        if (overrideLeaves || !leaves.isEmpty()) {
            return true;
        } else {
            return logFinder.find().anyMatch(p -> !leaves.isEmpty() || overrideLeaves);
        }
    }

    @Override
    public void setLogBlocks(Set<BlockPos> logBlocks) {
        logs = logBlocks;
        mass = ChopUtil.getSupportFactor(level, logs.stream()).orElse(1.0);
        logFinder = new LogFinder(a -> Stream.empty(), Collections.emptySet(), 0);
    }

    @Override
    public void setLeaves(boolean hasLeaves) {
        overrideLeaves = hasLeaves;
    }

    @Override
    public Stream<BlockPos> streamLogs() {
        return Stream.concat(logs.stream(), logFinder.find());
    }

    @Override
    public Stream<BlockPos> streamLeaves() {
        List<BlockPos> allLeaves = new LinkedList<>();

        forEachLeaves(leaves, allLeaves::add); // TODO: yikes; defeats the purpose of streaming

        return allLeaves.stream();
    }

    private void completeTree() {
        logFinder.find().count();
    }

    @Override
    public void forEachLeaves(Consumer<BlockPos> consumer) {
        forEachLeaves(leaves, consumer);
    }

    private void forEachLeaves(Collection<BlockPos> firstLeaves, Consumer<BlockPos> forEach) {
        completeTree(); // Make sure all log-adjacent leaves are discovered
        if (smartDetection) {
            forEachLeavesSmart(firstLeaves, forEach);
        } else {
            forEachLeavesDumb(firstLeaves, forEach);
        }
    }

    private void forEachLeavesSmart(Collection<BlockPos> firstLeaves, Consumer<BlockPos> forEach) {
        leaves.stream().filter(pos -> leavesHasExactDistance(level.getBlockState(pos), 1)).forEach(forEach);

        AtomicInteger highestDistance = new AtomicInteger(maxLeavesDistance);
        AtomicInteger distance = new AtomicInteger();
        DirectedGraph<BlockPos> distancedLeavesGraph = GraphUtil.filterNeighbors(
                leavesWorld,
                pos -> {
                    BlockState state = level.getBlockState(pos);
                    state.getOptionalValue(LeavesBlock.DISTANCE).ifPresent(d -> {
                        if (d > highestDistance.get()) {
                            highestDistance.set(d);
                        }
                    });

                    return leavesHasAtLeastDistance(state, distance.get());
                }
        );

        FloodFillImpl<BlockPos> flood = new FloodFillImpl<>(firstLeaves, distancedLeavesGraph, a -> 0);

        for (int i = 2; i <= highestDistance.get(); ++i) {
            distance.set(i);
            flood.fillOnce(forEach);
        }
    }

    private void forEachLeavesDumb(Collection<BlockPos> firstLeaves, Consumer<BlockPos> forEach) {
        FloodFillImpl<BlockPos> flood = new FloodFillImpl<>(firstLeaves, leavesWorld, a -> 0);

        for (int i = 0; i < maxLeavesDistance; ++i) {
            flood.fillOnce(forEach);
        }
    }

    @Override
    public boolean readyToFell(int numChops) {
        if (!ChopUtil.enoughChopsToFell(numChops, mass)) {
            return false;
        } else {
            return logFinder.find().allMatch(ignored -> ChopUtil.enoughChopsToFell(numChops, mass));
        }
    }

    @Override
    public int getChops() {
        return chops;
    }

    public Level getLevel() {
        return level;
    }

    private boolean leavesHasExactDistance(BlockState state, int distance) {
        return state.getOptionalValue(LeavesBlock.DISTANCE).orElse(distance) == distance || state.getOptionalValue(LeavesBlock.PERSISTENT).orElse(false);
    }

    private boolean leavesHasAtLeastDistance(BlockState state, int distance) {
        return state.getOptionalValue(LeavesBlock.DISTANCE).orElse(distance) >= distance || state.getOptionalValue(LeavesBlock.PERSISTENT).orElse(false);
    }

    private void makeTreeBase(Level level, BlockPos origin) {
        base = new HashSet<>();
        if (ChopUtil.isBlockChoppable(level, origin)) {
            DirectedGraph<BlockPos> adjacentWorld = BlockNeighbors.ADJACENTS_AND_DIAGONALS::asStream;
            base.add(origin);

            GraphUtil.flood(
                    GraphUtil.filterNeighbors(adjacentWorld, pos -> ChopUtil.getNumChops(level, pos) > 0),
                    origin,
                    Vec3i::getY
            ).fill().forEach(base::add);
        }
    }

    @Override
    public Collection<Chop> chop(BlockPos target, int numChops) {
        List<Chop> chops = new Stack<>();
        AtomicInteger chopsLeft = new AtomicInteger(numChops);

        if (chopsLeft.get() > 0) {
            GraphUtil.flood(logsWorld, base, a -> ChopUtil.blockDistance(target, a) * 32 + RandomUtils.nextInt(0, 32))
                    .fill()
                    .takeWhile(pos -> {
                        chopsLeft.set(gatherChopAndGetNumChopsRemaining(level, pos, chopsLeft.get(), chops));
                        return chopsLeft.get() > 0;
                    })
                    .count();
        }

        return chops;
    }

    private static int gatherChopAndGetNumChopsRemaining(Level level, BlockPos pos, int numChops, List<Chop> chops) {
        BlockState blockStateBeforeChopping = level.getBlockState(pos);

        if (!(blockStateBeforeChopping.getBlock() instanceof IChoppableBlock) && isBlockSurrounded(level, pos)) {
            return numChops;
        }

        int adjustedNumChops = adjustNumChops(level, pos, blockStateBeforeChopping, numChops);

        if (adjustedNumChops > 0) {
            chops.add(new Chop(pos, adjustedNumChops));
        }

        return numChops - adjustedNumChops;
    }

    private static int adjustNumChops(Level level, BlockPos blockPos, BlockState blockState, int numChops) {
        IChoppableBlock choppableBlock = ClassUtil.getChoppableBlock(level, blockPos, blockState);
        if (choppableBlock != null) {
            int currentNumChops = choppableBlock.getNumChops(level, blockPos, blockState);
            int maxNondestructiveChops = choppableBlock.getMaxNumChops(level, blockPos, blockState) - currentNumChops;
            return Math.min(maxNondestructiveChops, numChops);
        } else {
            return 0;
        }
    }

    private static boolean isBlockSurrounded(Level level, BlockPos pos) {
        return Stream.of(pos.west(), pos.north(), pos.east(), pos.south())
                .allMatch(neighborPos -> ChopUtil.isBlockALog(level, neighborPos));
    }

    private static class LogFinder {
        private int size = 0;
        private final int maxSize;
        FloodFill<BlockPos> flood;

        public LogFinder(DirectedGraph<BlockPos> logsWorld, Set<BlockPos> base, int maxSize) {
            flood = GraphUtil.flood(logsWorld, base, v -> -v.getY());
            this.maxSize = maxSize;
        }

        public Stream<BlockPos> find() {
            int n = size;
            return flood.fill().peek(a -> ++size).limit(maxSize - n);
        }
    }
}
