package ht.treechop.common.util;

import ht.treechop.api.AbstractTreeData;
import ht.treechop.common.config.ChopCounting;
import net.minecraft.core.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Deprecated
public class TreeDataImpl extends AbstractTreeData {
    private boolean hasLeaves;
    private Set<BlockPos> logBlocks;

    public TreeDataImpl() {
        logBlocks = new HashSet<>();
    }

    public TreeDataImpl(boolean overrideLeaves) {
        this();
        this.hasLeaves = overrideLeaves;
    }

    public static TreeDataImpl empty() {
        return new TreeDataImpl();
    }

    @Override
    public void setLogBlocks(Set<BlockPos> logBlocks) {
        this.logBlocks = logBlocks;
    }

    @Override
    public int getChops() {
        return 0;
    }

    @Override
    public Stream<BlockPos> streamLogs() {
        return logBlocks.stream();
    }

    @Override
    public Stream<BlockPos> streamLeaves() {
        return Stream.empty(); // TODO
    }

    @Override
    public boolean hasLeaves() {
        return hasLeaves;
    }

    @Override
    public boolean readyToFell(int numChops) {
        return numChops >= ChopCounting.calculate(logBlocks.size());
    }

    @Override
    public void setLeaves(boolean hasLeaves) {
        this.hasLeaves = hasLeaves;
    }

}
