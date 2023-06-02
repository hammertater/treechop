package ht.treechop.common.chop;

import ht.treechop.api.TreeData;
import ht.tuber.graph.DirectedGraph;
import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class LazyTreeData implements TreeData {

    public LazyTreeData(Collection<BlockPos> startBlocks, DirectedGraph<BlockPos> graph) {

    }

    @Override
    public boolean hasLeaves() {
        return false;
    }

    @Override
    public void setLogBlocks(Set<BlockPos> logBlocks) {

    }

    @Override
    public void setLeaves(boolean hasLeaves) {

    }

    @Override
    public Optional<Set<BlockPos>> getLogBlocks() {
        return Optional.empty();
    }

    @Override
    public Set<BlockPos> getLogBlocksOrEmpty() {
        return null;
    }

    @Override
    public boolean isAProperTree(boolean mustHaveLeaves) {
        return false;
    }
}
