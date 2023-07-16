package ht.treechop.common.chop;

import ht.treechop.api.AbstractTreeData;
import ht.treechop.common.config.ChopCounting;
import ht.tuber.graph.DirectedGraph;
import ht.tuber.graph.FloodFill;
import ht.tuber.graph.FloodFillImpl;
import ht.tuber.graph.GraphUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class LazyTreeData extends AbstractTreeData {

    private final int chops;
    private Set<BlockPos> logs;
    private final Set<BlockPos> leaves;
    private FloodFill<BlockPos> generator;
    private boolean overrideLeaves = false;

    public LazyTreeData(Collection<BlockPos> origin, DirectedGraph<BlockPos> graph, Predicate<BlockPos> logFilter, Predicate<BlockPos> leavesFilter, int maxNumTreeBlocks, int chops) {
        leaves = new HashSet<>();
        logs = new HashSet<>();
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
        if (ChopCounting.calculate(logs.size()) > numChops) {
            return false;
        } else {
            return generator.fill().allMatch(ignored -> ChopCounting.calculate(logs.size()) <= numChops);
        }
    }

    @Override
    public int getChops() {
        return chops;
    }
}
