package ht.treechop.api;

import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.Set;

public interface TreeDataImmutable {
    Optional<Set<BlockPos>> getLogBlocks();

    Set<BlockPos> getLogBlocksOrEmpty();

    boolean isAProperTree(boolean mustHaveLeaves);
}
