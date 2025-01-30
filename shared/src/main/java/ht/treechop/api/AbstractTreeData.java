package ht.treechop.api;

import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.FellLeavesStrategy;
import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractTreeData implements TreeData {
    @Override
    public boolean isAProperTree(boolean mustHaveLeaves) {
        boolean canBeLil = hasLeaves() && ConfigHandler.COMMON.fellLeavesStrategy.get() != FellLeavesStrategy.IGNORE;
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

    @Override
    public void forEachLeaves(Consumer<BlockPos> consumer) {
        streamLeaves().forEach(consumer);
    }
}
