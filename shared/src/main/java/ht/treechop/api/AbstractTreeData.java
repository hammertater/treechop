package ht.treechop.api;

import ht.treechop.common.config.ConfigHandler;
import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractTreeData implements TreeData {
    @Override
    public boolean isAProperTree(boolean mustHaveLeaves) {
        boolean canBeLil = hasLeaves() && ConfigHandler.COMMON.breakLeaves.get();
        long lowerboundSize = streamLogs().limit(2).count();
        return (hasLeaves() || !mustHaveLeaves) && lowerboundSize >= (canBeLil ? 1 : 2);
    }

    @Override
    public Optional<Set<BlockPos>> getLogBlocks() {
        return Optional.of(getLogBlocksOrEmpty());
    }

    @Override
    public Set<BlockPos> getLogBlocksOrEmpty() {
        return streamLogs().collect(Collectors.toSet());
    }
}
