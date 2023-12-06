package ht.treechop.common.config;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface TreeLeavesBehavior {
    boolean isLeaves(BlockState blockState);

    TreeLeavesBehavior DEFAULT = state -> !ConfigHandler.COMMON.ignorePersistentLeaves.get() || !state.hasProperty(LeavesBlock.PERSISTENT) || !state.getValue(LeavesBlock.PERSISTENT);
    TreeLeavesBehavior PROBLEMATIC = state -> true;
}
