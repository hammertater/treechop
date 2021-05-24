package ht.treechop.common.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChopEvent extends Event {

    private final World world;
    private final EntityPlayer player;
    private final BlockPos choppedBlockPos;
    private final IBlockState choppedIBlockState;

    public ChopEvent(World world, EntityPlayer player, BlockPos choppedBlockPos, IBlockState choppedIBlockState) {
        this.world = world;
        this.player = player;
        this.choppedBlockPos = choppedBlockPos;
        this.choppedIBlockState = choppedIBlockState;
    }

    public World getWorld() {
        return world;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public BlockPos getChoppedBlockPos() {
        return choppedBlockPos;
    }

    public IBlockState getChoppedIBlockState() {
        return choppedIBlockState;
    }

    @Cancelable
    public static class DetectTreeEvent extends ChopEvent {
        private final AtomicBoolean hasLeaves;
        private final AtomicBoolean overrideHasLeaves;

        public DetectTreeEvent(World world, EntityPlayer player, BlockPos choppedBlockPos, IBlockState choppedIBlockState, AtomicBoolean hasLeaves, AtomicBoolean overrideHasLeaves) {
            super(world, player, choppedBlockPos, choppedIBlockState);
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

        public StartChopEvent(BlockEvent.BreakEvent breakEvent, World world, EntityPlayer player, BlockPos choppedBlockPos, IBlockState choppedIBlockState, int numChops, boolean felling) {
            super(world, player, choppedBlockPos, choppedIBlockState);
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
        public FinishChopEvent(World world, EntityPlayer player, BlockPos choppedBlockPos, IBlockState choppedIBlockState) {
            super(world, player, choppedBlockPos, choppedIBlockState);
        }
    }

}
