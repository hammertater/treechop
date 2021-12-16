package ht.treechop.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class WorldBlock {
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private final BlockEntity tileEntity;

    public WorldBlock(Level level, BlockPos pos, BlockState state, BlockEntity tileEntity) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.tileEntity = tileEntity;
    }

    public WorldBlock(Level level, BlockPos pos, BlockState state) {
        this(level, pos, state, null);
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public BlockEntity getTileEntity() {
        return tileEntity;
    }

    public Level getWorld() {
        return level;
    }

    public static WorldBlock fromWorld(Level level, BlockPos pos) {
        return new WorldBlock(level, pos, level.getBlockState(pos), level.getBlockEntity(pos));
    }

    public static List<WorldBlock> fromWorld(Level level, Collection<BlockPos> positions) {
        return positions.stream()
                .map(pos -> fromWorld(level, pos))
                .collect(Collectors.toList());
    }
}
