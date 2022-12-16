package ht.treechop.common.util;

import ht.treechop.TreeChopMod;
import ht.treechop.api.IChoppableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

    public void apply(World level, PlayerEntity player, ItemStack tool, boolean felling) {
        BlockState blockState = level.getBlockState(blockPos);
        Block block = ChopUtil.getChoppedBlock(blockState);
        TreeChopMod.LOGGER.warn(String.format("y0: block = %s, chopped block = %s (%s)", blockState.toString(), block.toString(), block.getClass().getName()));
        if (block instanceof IChoppableBlock) {
            TreeChopMod.LOGGER.warn(String.format("y1 %d %d", numChops, felling ? 1 : 0));
            ((IChoppableBlock) block).chop(player, tool, level, blockPos, blockState, numChops, felling);
        } else {
            TreeChopMod.LOGGER.warn("y2");
            TreeChopMod.LOGGER.warn("Failed to chop block in level {} at position {} for player {}: {} does not implement IChoppableBlock", level.dimension(), blockPos, player.getName(), blockState.getBlock().getRegistryName());
        }
        TreeChopMod.LOGGER.warn("y3");
    }
}
