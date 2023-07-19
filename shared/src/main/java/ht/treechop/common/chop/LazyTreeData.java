package ht.treechop.common.chop;

import ht.treechop.api.AbstractTreeData;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.BlockNeighbors;
import ht.tuber.graph.DirectedGraph;
import ht.tuber.graph.FloodFill;
import ht.tuber.graph.FloodFillImpl;
import ht.tuber.graph.GraphUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LazyTreeData extends AbstractTreeData {

    private final Level level;
    private final int chops;
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

    private FloodFill<BlockPos> generator;

    public LazyTreeData(Level level, Collection<BlockPos> base, DirectedGraph<BlockPos> logGraph, Predicate<BlockPos> logFilter, Predicate<BlockPos> leavesFilter, int maxNumTreeBlocks, int chops) {
        this.level = level;
        this.chops = chops;
        logs.addAll(base);

        DirectedGraph<BlockPos> world = GraphUtil.filter(
                logGraph,
                this::gatherLog,
                pos -> check(pos, logFilter, leavesFilter)
        );
        generator = GraphUtil.flood(world, base, Vec3i::getY);
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
            return generator.fill().anyMatch(p -> !leaves.isEmpty() || overrideLeaves);
        }
    }

    @Override
    public void setLogBlocks(Set<BlockPos> logBlocks) {
        logs = logBlocks;
        mass = ChopUtil.getSupportFactor(level, logs.stream()).orElse(1.0);
        generator = new FloodFillImpl<>(List.of(), a -> Stream.empty(), a -> 0);
    }

    @Override
    public void setLeaves(boolean hasLeaves) {
        overrideLeaves = hasLeaves;
    }

    @Override
    public Stream<BlockPos> streamLogs() {
        return Stream.concat(logs.stream(), generator.fill());
    }

    @Override
    public Stream<BlockPos> streamLeaves() {
        streamLogs().forEach(a -> {}); // TODO: don't do this? Makes sure all log-adjacent leaves are discovered

        List<BlockPos> allLeaves = new LinkedList<>();
        forEachLeaves(allLeaves, allLeaves::add); // TODO: yikes; defeats the purpose of streaming

        return allLeaves.stream();
    }

    private void forEachLeaves(List<BlockPos> firstLeaves, Consumer<BlockPos> forEach) {
        leaves.stream().filter(pos -> leavesHasExactDistance(level.getBlockState(pos), 1)).forEach(forEach);

        AtomicInteger distance = new AtomicInteger();
        DirectedGraph<BlockPos> distancedLeavesGraph = GraphUtil.filterNeighbors(
                BlockNeighbors.ADJACENTS::asStream,
                pos -> {
                    BlockState state = level.getBlockState(pos);
                    return ChopUtil.isBlockLeaves(state) && (leavesHasAtLeastDistance(state, distance.get()));
                }
        );

        FloodFillImpl<BlockPos> flood = new FloodFillImpl<>(firstLeaves, distancedLeavesGraph, a -> 0);

        int maxDistance = ConfigHandler.COMMON.maxBreakLeavesDistance.get();
        for (int i = 2; i < maxDistance; ++i) {
            distance.set(i);
            flood.fillOnce(forEach);
        }
    }

    @Override
    public boolean readyToFell(int numChops) {
        if (!ChopUtil.enoughChopsToFell(numChops, mass)) {
            return false;
        } else {
            return generator.fill().allMatch(ignored -> ChopUtil.enoughChopsToFell(numChops, mass));
        }
    }

    @Override
    public int getChops() {
        return chops;
    }

    public Level getLevel() {
        return level;
    }

    public Collection<BlockPos> getIncompleteLogs() {
        return logs;
    }

    public Collection<BlockPos> getIncompleteLeaves() {
        return leaves;
    }

    private boolean leavesHasExactDistance(BlockState state, int distance) {
        return (state.getOptionalValue(LeavesBlock.PERSISTENT).orElse(true))
                ? true
                : state.getOptionalValue(LeavesBlock.DISTANCE).orElse(distance) == distance;
    }

    private boolean leavesHasAtLeastDistance(BlockState state, int distance) {
        return (state.getOptionalValue(LeavesBlock.PERSISTENT).orElse(true))
                ? true
                : state.getOptionalValue(LeavesBlock.DISTANCE).orElse(distance) >= distance;
    }
}
