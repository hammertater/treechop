package ht.treechop.api;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.Optional;

/**
 * All events triggered by TreeChop extend {@code ChopEvent}. Listeners can alter how and when chopping occurs.
 */
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

    /**
     * Signals that a tree is about to be felled. Fires before any blocks are broken.
     * Can be canceled to prevent felling.
     */
    @Cancelable
    public static class BeforeFellEvent extends ChopEvent {
        private final FellData fellData;

        public BeforeFellEvent(Level level, ServerPlayer player, BlockPos blockPos, BlockState blockState, FellData fellData) {
            super(level, player, blockPos, blockState);
            this.fellData = fellData;
        }

        public FellData getFellData() {
            return fellData;
        }
    }

    /**
     * Signals that a tree has been felled.
     */
    @Cancelable
    public static class AfterFellEvent extends ChopEvent {
        private final FellData fellData;

        public AfterFellEvent(Level level, ServerPlayer player, BlockPos blockPos, BlockState blockState, FellData fellData) {
            super(level, player, blockPos, blockState);
            this.fellData = fellData;
        }

        public FellData getFellData() {
            return fellData;
        }
    }

    /**
     * Used to determine whether a choppable block belongs to a tree. For example,
     * <ul>
     *   <li> to trigger chopping when a choppable block is broken
     *   <li> to activate the on-screen chop indicator when the player highlights a choppable block
     *   <li> to add tree information to Jade/WTHIT/TheOneProbe/etc. popups
     * </ul>
     * Can be canceled to prevent tree detection. Note that detection events only trigger for blocks that are considered choppable (see {@link TreeChopAPI#isBlockChoppable}).
     */
    @Cancelable
    public static class DetectTreeEvent extends ChopEvent {
        private TreeData treeData;

        public DetectTreeEvent(Level level, ServerPlayer player, BlockPos blockPos, BlockState blockState, TreeData treeData) {
            super(level, player, blockPos, blockState);
            this.treeData = treeData;
        }

        @Deprecated
        public void overrideTreeHasLeaves(boolean hasLeaves) {
            treeData.setLeaves(hasLeaves);
        }

        public Optional<TreeData> getTreeData() {
            return Optional.ofNullable(treeData);
        }

        public void setTreeData(TreeData treeData) {
            this.treeData = treeData;
        }
    }

    /**
     * Signals that a block is about to be chopped. Fires after a player breaks a choppable block (see {@link
     * TreeChopAPI#isBlockChoppable}), but before it is replaced by a chopped block (usually a {@code
     * treechop:chopped_log}). Can be canceled to prevent chopping.
     */
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

        public void setNumChops(int numChops) {
            chopData.setNumChops(numChops);
        }

        public boolean getFelling() {
            return chopData.getFelling();
        }

        public void setFelling(boolean felling) {
            chopData.setFelling(felling);
        }

        public Object getTrigger() {
            return trigger;
        }
    }

    /**
     * Signals that a block has been chopped.
     */
    public static class FinishChopEvent extends ChopEvent {
        private final ChopDataImmutable chopData;
        private final boolean felled;

        public FinishChopEvent(Level level, Player player, BlockPos choppedBlockPos, BlockState choppedBlockState, ChopDataImmutable chopData, boolean felled) {
            super(level, player, choppedBlockPos, choppedBlockState);
            this.chopData = chopData;
            this.felled = felled;
        }

        public int getNumChops() {
            return chopData.getNumChops();
        }

        public boolean getFelling() {
            return chopData.getFelling();
        }

        public boolean getFelled() {
            return felled;
        }
    }

}
