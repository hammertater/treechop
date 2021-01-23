package ht.treechop.common.event;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

public class ChopEvent extends Event {

    private final World world;
    private final PlayerEntity player;
    private final BlockPos choppedBlockPos;
    private final BlockState choppedBlockState;

    public ChopEvent(World world, PlayerEntity player, BlockPos choppedBlockPos, BlockState choppedBlockState) {
        this.world = world;
        this.player = player;
        this.choppedBlockPos = choppedBlockPos;
        this.choppedBlockState = choppedBlockState;
    }

    public World getWorld() {
        return world;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public BlockPos getChoppedBlockPos() {
        return choppedBlockPos;
    }

    public BlockState getChoppedBlockState() {
        return choppedBlockState;
    }

    @Cancelable
    public static class StartChopEvent extends ChopEvent {
        public StartChopEvent(World world, PlayerEntity player, BlockPos choppedBlockPos, BlockState choppedBlockState) {
            super(world, player, choppedBlockPos, choppedBlockState);
        }
    }

    public static class FinishChopEvent extends ChopEvent {
        public FinishChopEvent(World world, PlayerEntity player, BlockPos choppedBlockPos, BlockState choppedBlockState) {
            super(world, player, choppedBlockPos, choppedBlockState);
        }
    }

    @Cancelable
    public static class FellEvent extends ChopEvent {
        public FellEvent(World world, PlayerEntity player, BlockPos choppedBlockPos, BlockState choppedBlockState) {
            super(world, player, choppedBlockPos, choppedBlockState);
        }
    }

}
