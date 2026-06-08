package ht.treechop.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class LevelUtil {
    public static void harvestBlock(
            Entity agent,
            Level level,
            BlockPos pos,
            ItemStack tool,
            boolean loud
    ) {
        harvestBlock(agent, level, pos, pos, tool, loud);
    }

    public static void harvestBlock(
            Entity agent,
            Level level,
            BlockPos pos,
            BlockPos dropPos,
            ItemStack tool,
            boolean loud
    ) {
        if (level instanceof ServerLevel serverLevel) {
            BlockState blockState = level.getBlockState(pos);

            // Plays particle and sound effects
            if (loud && agent instanceof Player player) {
                blockState.getBlock().playerWillDestroy(level, pos, blockState, player);
            }

            FluidState fluidStateOrAir = level.getFluidState(pos);
            blockState.getBlock().destroy(level, pos, blockState);
            if (dropPos.equals(pos)) {
                Block.dropResources(blockState, level, pos, level.getBlockEntity(pos), agent, tool); // Should drop XP
            } else {
                // Group drop: compute drops at the real pos but spawn them at dropPos
                Block.getDrops(blockState, serverLevel, pos, level.getBlockEntity(pos), agent, tool)
                        .forEach(stack -> Block.popResource(level, dropPos, stack));
            }
            level.setBlockAndUpdate(pos, fluidStateOrAir.createLegacyBlock());
        }
    }
}
