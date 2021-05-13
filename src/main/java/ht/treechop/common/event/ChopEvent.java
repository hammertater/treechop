package ht.treechop.common.event;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.concurrent.atomic.AtomicBoolean;

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
    public static class DetectTreeEvent extends ChopEvent {
        private final AtomicBoolean hasLeaves;
        private final AtomicBoolean overrideHasLeaves;

        public DetectTreeEvent(World world, PlayerEntity player, BlockPos choppedBlockPos, BlockState choppedBlockState, AtomicBoolean hasLeaves, AtomicBoolean overrideHasLeaves) {
            super(world, player, choppedBlockPos, choppedBlockState);
            this.overrideHasLeaves = overrideHasLeaves;
            this.hasLeaves = hasLeaves;
        }

        public void overrideTreeHasLeaves(boolean hasLeaves) {
            this.hasLeaves.set(hasLeaves);
            overrideHasLeaves.set(true);
        }
    }

    @Cancelable
    public static class StartChopEvent extends ChopEvent {
        private BlockEvent.BreakEvent breakEvent;
        private int numChops;
        private boolean felling;

        public StartChopEvent(BlockEvent.BreakEvent breakEvent, World world, PlayerEntity player, BlockPos choppedBlockPos, BlockState choppedBlockState, int numChops, boolean felling) {
            super(world, player, choppedBlockPos, choppedBlockState);
            this.breakEvent = breakEvent;
            this.numChops = numChops;
            this.felling = felling;
        }

        public BlockEvent.BreakEvent getBreakEvent() {
            return breakEvent;
        }

        public int getNumChops() {
            return numChops;
        }

        public boolean getFelling() {
            return felling;
        }

        public void setNumChops(int numChops) {
            this.numChops = numChops;
        }

        public void setFelling(boolean felling) {
            this.felling = felling;
        }
    }

    public static class FinishChopEvent extends ChopEvent {
        public FinishChopEvent(World world, PlayerEntity player, BlockPos choppedBlockPos, BlockState choppedBlockState) {
            super(world, player, choppedBlockPos, choppedBlockState);
        }
    }

}
