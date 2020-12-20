package ht.treechop.common.util;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockThatWasBroken {
    public static final BlockThatWasBroken IGNORED = new BlockThatWasBroken(null, null, null, false);
    private final BlockPos blockPos;
    private final BlockState blockState;
    private final TileEntity tileEntity;
    private final boolean canHarvest;

    public BlockThatWasBroken(BlockPos choppedPos, BlockState choppedState, TileEntity tileEntity, boolean canHarvest) {
        this.blockPos = choppedPos;
        this.blockState = choppedState;
        this.tileEntity = tileEntity;
        this.canHarvest = canHarvest;
    }

    public BlockThatWasBroken(World world, BlockPos pos, BlockState blockState, PlayerEntity agent) {
        this(pos, blockState, world.getTileEntity(pos), blockState.getBlock().canHarvestBlock(blockState, world, pos, agent));
    }

    public BlockThatWasBroken(World world, BlockPos pos, PlayerEntity agent) {
        this(world, pos, world.getBlockState(pos), agent);
    }

    public BlockPos getPos() { return blockPos; }
    public BlockState getState() { return blockState; }
    public TileEntity getTileEntity() { return tileEntity; }
    public boolean canHarvest() { return canHarvest; }
}
