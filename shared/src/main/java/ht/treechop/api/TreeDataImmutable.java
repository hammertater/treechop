package ht.treechop.api;

import net.minecraft.core.BlockPos;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public interface TreeDataImmutable {
    Optional<Set<BlockPos>> getLogBlocks();

    Set<BlockPos> getLogBlocksOrEmpty();

    Stream<BlockPos> streamLogBlocks();

    boolean isAProperTree(boolean mustHaveLeaves);
}
