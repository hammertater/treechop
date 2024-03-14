package ht.treechop.common.util;

import ht.treechop.api.AbstractTreeData;
import ht.treechop.common.chop.Chop;
import ht.treechop.common.chop.ChopUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Deprecated
public class TreeDataImpl extends AbstractTreeData {
    private final Level level;
    private int chopsToFell;
    private boolean hasLeaves;
    private Set<BlockPos> logBlocks = new HashSet<>();

    public TreeDataImpl(Level level, boolean overrideLeaves) {
        this.level = level;
        this.hasLeaves = overrideLeaves;
    }

    public TreeDataImpl(Level level) {
        this(level, false);
    }

    public static TreeDataImpl empty(Level level) {
        return new TreeDataImpl(level);
    }

    @Override
    public void setLogBlocks(Set<BlockPos> logBlocks) {
        this.logBlocks = logBlocks;
        this.chopsToFell = ChopUtil.numChopsToFell(level, logBlocks.stream());
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
        return ChopUtil.enoughChopsToFell(numChops, chopsToFell);
    }

    @Override
    public int numChopsNeededToFell() {
        return chopsToFell;
    }

    @Override
    public Collection<Chop> chop(BlockPos target, int numChops) {
        return Collections.emptyList();
    }

    @Override
    public void setLeaves(boolean hasLeaves) {
        this.hasLeaves = hasLeaves;
    }

}
