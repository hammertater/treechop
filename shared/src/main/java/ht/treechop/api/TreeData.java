package ht.treechop.api;

import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

public class TreeData {
    private boolean hasLeaves;
    private Set<BlockPos> logBlocks;

    public <T> TreeData() {
        logBlocks = null;
    }

    public TreeData(boolean overrideLeaves) {
        this();
        this.hasLeaves = overrideLeaves;
    }

    public static TreeData empty() {
        return new TreeData();
    }

    public Optional<Set<BlockPos>> getLogBlocks() {
        return Optional.ofNullable(logBlocks);
    }

    public void setLogBlocks(Set<BlockPos> logBlocks) {
        this.logBlocks = logBlocks;
    }

    public Set<BlockPos> getLogBlocksOrEmpty() {
        return getLogBlocks().orElse(Collections.emptySet());
    }

    public boolean hasLeaves() {
        return hasLeaves;
    }

    public void setLeaves(boolean hasLeaves) {
        this.hasLeaves = hasLeaves;
    }
}
