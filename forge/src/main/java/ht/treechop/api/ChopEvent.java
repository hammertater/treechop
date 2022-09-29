package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

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
        private final TreeData treeData;

        public DetectTreeEvent(Level level, ServerPlayer player, BlockPos choppedBlockPos, BlockState choppedBlockState, TreeData treeData) {
            super(level, player, choppedBlockPos, choppedBlockState);
            this.treeData = treeData;
        }

        public void overrideTreeHasLeaves(boolean hasLeaves) {
            treeData.setLeaves(hasLeaves);
        }
    }

    @Cancelable
    public static class StartChopEvent extends ChopEvent {
        private final Object trigger;
        private final ChopData chopData;

        public StartChopEvent(Level level, ServerPlayer player, BlockPos choppedBlockPos, BlockState choppedBlockState, ChopData chopData) {
            this(level, player, choppedBlockPos, choppedBlockState, chopData, null);
        }

        public StartChopEvent(Level level, ServerPlayer player, BlockPos choppedBlockPos, BlockState choppedBlockState, ChopData chopData, Object trigger) {
            super(level, player, choppedBlockPos, choppedBlockState);
            this.chopData = chopData;
            this.trigger = trigger;
        }

        public int getNumChops() {
            return chopData.getNumChops();
        }

        public boolean getFelling() {
            return chopData.getFelling();
        }

        public void setNumChops(int numChops) {
            chopData.numChops = numChops;
        }

        public void setFelling(boolean felling) {
            chopData.felling = felling;
        }

        public Object getTrigger() {
            return trigger;
        }
    }

    public static class FinishChopEvent extends ChopEvent {
        public FinishChopEvent(Level level, Player player, BlockPos choppedBlockPos, BlockState choppedBlockState) {
            super(level, player, choppedBlockPos, choppedBlockState);
        }
    }

}
