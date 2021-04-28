package ht.treechop.common.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

public class ChopEvent extends Event {

    private final World world;
    private final EntityPlayer player;
    private final BlockPos choppedBlockPos;
    private final IBlockState choppedBlockState;

    public ChopEvent(World world, EntityPlayer player, BlockPos choppedBlockPos, IBlockState choppedBlockState) {
        this.world = world;
        this.player = player;
        this.choppedBlockPos = choppedBlockPos;
        this.choppedBlockState = choppedBlockState;
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

    public IBlockState getChoppedBlockState() {
        return choppedBlockState;
    }

    public static class StartChopEvent extends ChopEvent {
        private BlockEvent.BreakEvent breakEvent;

        public StartChopEvent(BlockEvent.BreakEvent breakEvent, World world, EntityPlayer player, BlockPos choppedBlockPos, IBlockState choppedBlockState) {
            super(world, player, choppedBlockPos, choppedBlockState);
            this.breakEvent = breakEvent;
        }

        public BlockEvent.BreakEvent getBreakEvent() {
            return breakEvent;
        }
    }

    public static class FinishChopEvent extends ChopEvent {
        public FinishChopEvent(World world, EntityPlayer player, BlockPos choppedBlockPos, IBlockState choppedBlockState) {
            super(world, player, choppedBlockPos, choppedBlockState);
        }
    }

    public static class FellEvent extends ChopEvent {
        public FellEvent(World world, EntityPlayer player, BlockPos choppedBlockPos, IBlockState choppedBlockState) {
            super(world, player, choppedBlockPos, choppedBlockState);
        }
    }

}
