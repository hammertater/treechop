package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChopEvent extends Event {

    private final Level level;
    private final Player player;
    private final BlockPos choppedBlockPos;
    private final BlockState choppedBlockState;

    public ChopEvent(Level level, Player player, BlockPos choppedBlockPos, BlockState choppedBlockState) {
        this.level = level;
        this.player = player;
        this.choppedBlockPos = choppedBlockPos;
        this.choppedBlockState = choppedBlockState;
    }

    public Level getLevel() {
        return level;
    }

    public Player getPlayer() {
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

        public DetectTreeEvent(Level level, Player player, BlockPos choppedBlockPos, BlockState choppedBlockState, AtomicBoolean hasLeaves, AtomicBoolean overrideHasLeaves) {
            super(level, player, choppedBlockPos, choppedBlockState);
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

        public StartChopEvent(BlockEvent.BreakEvent breakEvent, Level level, ServerPlayer player, BlockPos choppedBlockPos, BlockState choppedBlockState, int numChops, boolean felling) {
            super(level, player, choppedBlockPos, choppedBlockState);
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
        public FinishChopEvent(Level level, Player player, BlockPos choppedBlockPos, BlockState choppedBlockState) {
            super(level, player, choppedBlockPos, choppedBlockState);
        }
    }

}
