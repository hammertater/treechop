package ht.treechop.api;

import net.minecraft.core.BlockPos;

import java.util.Set;

public interface TreeData extends TreeDataImmutable {

    @Deprecated
    void setLogBlocks(Set<BlockPos> logBlocks);

    @Deprecated
    void setLeaves(boolean hasLeaves);
}
