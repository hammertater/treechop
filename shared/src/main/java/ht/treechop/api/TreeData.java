package ht.treechop.api;

import net.minecraft.core.BlockPos;

import java.util.Set;

public interface TreeData extends TreeDataImmutable {
    boolean hasLeaves();

    void setLogBlocks(Set<BlockPos> logBlocks);

    void setLeaves(boolean hasLeaves);
}
