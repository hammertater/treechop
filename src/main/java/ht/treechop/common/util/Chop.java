package ht.treechop.common.util;

import ht.treechop.TreeChopMod;
import ht.treechop.api.IChoppableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class Chop {
    private final BlockPos blockPos;
    private final int numChops;

    public Chop(BlockPos blockPos, int numChops) {
        this.blockPos = blockPos;
        this.numChops = numChops;
    }

    public int getNumChops() {
        return numChops;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void apply(Level level, Player player, ItemStack tool, boolean felling) {
        BlockState blockState = level.getBlockState(blockPos);
        Block block = ChopUtil.getChoppedBlock(blockState);
        if (block instanceof IChoppableBlock choppableBlock) {
            choppableBlock.chop(player, tool, level, blockPos, blockState, numChops, felling);
        } else {
            TreeChopMod.LOGGER.warn("Failed to chop block in level {} at position {} for player {}: {} does not implement IChoppableBlock", level.dimension(), blockPos, player.getName(), blockState.getBlock().getRegistryName());
        }
    }
}
