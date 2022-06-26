package ht.treechop.common.util;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class WorldBlock {
    private final World world;
    private final BlockPos pos;
    private final BlockState state;
    private final TileEntity tileEntity;

    public WorldBlock(World world, BlockPos pos, BlockState state, TileEntity tileEntity) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.tileEntity = tileEntity;
    }

    public WorldBlock(World world, BlockPos pos, BlockState state) {
        this(world, pos, state, null);
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public World getWorld() {
        return world;
    }

    public static WorldBlock fromWorld(World world, BlockPos pos) {
        return new WorldBlock(world, pos, world.getBlockState(pos), world.getBlockEntity(pos));
    }

    public static List<WorldBlock> fromWorld(World world, Collection<BlockPos> positions) {
        return positions.stream()
                .map(pos -> fromWorld(world, pos))
                .collect(Collectors.toList());
    }
}
