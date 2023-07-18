package ht.treechop.common.chop;

import ht.treechop.api.AbstractTreeData;
import ht.tuber.graph.DirectedGraph;
import ht.tuber.graph.FloodFill;
import ht.tuber.graph.FloodFillImpl;
import ht.tuber.graph.GraphUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private final Set<BlockPos> leaves = new HashSet<>();

    private FloodFill<BlockPos> generator;

    public LazyTreeData(Level level, Collection<BlockPos> origin, DirectedGraph<BlockPos> graph, Predicate<BlockPos> logFilter, Predicate<BlockPos> leavesFilter, int maxNumTreeBlocks, int chops) {
        this.level = level;
        this.chops = chops;

        DirectedGraph<BlockPos> world = GraphUtil.filter(graph, pos -> check(pos, logFilter, leavesFilter));
        generator = GraphUtil.flood(world, origin, Vec3i::getY);
    }

    private boolean check(BlockPos pos, Predicate<BlockPos> logFilter, Predicate<BlockPos> leavesFilter) {
        if (logFilter.test(pos)) {
            logs.add(pos);
            if (leavesFilter.test(pos)) {
                setLeaves(true);
            }
            return true;
        } else {
            if (leavesFilter.test(pos)) {
                leaves.add(pos);
            }
            return false;
        }
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
        generator.fill().forEach(a -> {}); // TODO: don't do this
        return leaves.stream();
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
}
