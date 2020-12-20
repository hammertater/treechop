package ht.treechop.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockThatWasBroken {
    public static final BlockThatWasBroken IGNORED = new BlockThatWasBroken(null, null, null, false);
    private final BlockPos blockPos;
    private final IBlockState blockState;
    private final TileEntity tileEntity;
    private final boolean canHarvest;

    public BlockThatWasBroken(BlockPos choppedPos, IBlockState choppedState, TileEntity tileEntity, boolean canHarvest) {
        this.blockPos = choppedPos;
        this.blockState = choppedState;
        this.tileEntity = tileEntity;
        this.canHarvest = canHarvest;
    }

    public BlockThatWasBroken(World world, BlockPos pos, IBlockState blockState, EntityPlayer agent) {
        this(pos, blockState, world.getTileEntity(pos), blockState.getBlock().canHarvestBlock(world, pos, agent));
    }

    public BlockThatWasBroken(World world, BlockPos pos, EntityPlayer agent) {
        this(world, pos, world.getBlockState(pos), agent);
    }

    public BlockPos getPos() { return blockPos; }
    public IBlockState getState() { return blockState; }
    public TileEntity getTileEntity() { return tileEntity; }
    public boolean canHarvest() { return canHarvest; }
}
