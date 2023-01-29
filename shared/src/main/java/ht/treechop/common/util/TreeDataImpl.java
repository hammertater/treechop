package ht.treechop.common.util;

import ht.treechop.api.TreeData;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class TreeDataImpl implements TreeData {
    private boolean hasLeaves;
    private Set<BlockPos> logBlocks;

    public TreeDataImpl() {
        logBlocks = null;
    }

    public TreeDataImpl(boolean overrideLeaves) {
        this();
        this.hasLeaves = overrideLeaves;
    }

    public static TreeDataImpl empty() {
        return new TreeDataImpl();
    }

    @Override
    public Optional<Set<BlockPos>> getLogBlocks() {
        return Optional.ofNullable(logBlocks);
    }

    @Override
    public void setLogBlocks(Set<BlockPos> logBlocks) {
        this.logBlocks = logBlocks;
    }

    @Override
    public Set<BlockPos> getLogBlocksOrEmpty() {
        return getLogBlocks().orElse(Collections.emptySet());
    }

    @Override
    public boolean hasLeaves() {
        return hasLeaves;
    }

    @Override
    public void setLeaves(boolean hasLeaves) {
        this.hasLeaves = hasLeaves;
    }

    @Override
    public boolean isAProperTree(boolean mustHaveLeaves) {
        return (hasLeaves || !mustHaveLeaves) && getLogBlocksOrEmpty().size() >= ((hasLeaves && ConfigHandler.COMMON.breakLeaves.get()) ? 1 : 2);
    }
}
