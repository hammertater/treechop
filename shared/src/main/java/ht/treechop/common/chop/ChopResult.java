package ht.treechop.common.chop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface ChopResult {
    ChopResult IGNORED = (targetPos, agent, tool, breakLeaves) -> {};

    /**
     * Applies the results of chopping to the level, spawning the appropriate drops.
     * - Chopped blocks: harvest by agent, change to chopped state
     * - Felled blocks: harvest by no one, change to felled state
     * - Chopped and felled blocks: harvest by agent, change to felled state
     */
    void apply(BlockPos targetPos, ServerPlayer agent, ItemStack tool, boolean breakLeaves);
}
