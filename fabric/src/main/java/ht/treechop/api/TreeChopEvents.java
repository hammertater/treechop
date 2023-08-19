package ht.treechop.api;

import ht.treechop.common.util.TreeDataImpl;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Events used by TreeChop. Listeners can alter how and when chopping occurs.
 */
public final class TreeChopEvents {

    public static final Event<BeforeChop> BEFORE_CHOP = EventFactory.createArrayBacked(BeforeChop.class,
            (listeners) -> (world, player, pos, state, chopData) -> {
                for (BeforeChop listener : listeners) {
                    if (!listener.beforeChop(world, player, pos, state, chopData)) {
                        return false;
                    }
                }

                return true;
            }
    );

    public static final Event<AfterChop> AFTER_CHOP = EventFactory.createArrayBacked(AfterChop.class,
            (listeners) -> (world, player, pos, state, chopData, felled) -> {
                for (AfterChop event : listeners) {
                    event.afterChop(world, player, pos, state, chopData, felled);
                }
            }
    );

    public static final Event<DetectTree> DETECT_TREE = EventFactory.createArrayBacked(DetectTree.class,
            (listeners) -> (world, player, pos, state, treeData) -> {
                for (DetectTree listener : listeners) {
                    treeData = listener.onDetectTree(world, player, pos, state, treeData);
                    if (treeData == null) {
                        return TreeDataImpl.empty(world);
                    }
                }
                return treeData;
            }
    );

    private TreeChopEvents() {
    }

    @FunctionalInterface
    public interface BeforeChop {
        /**
         * Signals that a block is about to be chopped. Fires after a player breaks a choppable block (see {@link
         * TreeChopAPI#isBlockChoppable}), but before it is replaced by a chopped block (usually a {@code
         * treechop:chopped_log}).
         *
         * @param chopData Stores the number of chops to perform and whether to fell if possible
         * @return {@code false} to prevent chopping and instead break the block normally
         */
        boolean beforeChop(Level world, Player player, BlockPos pos, BlockState state, ChopData chopData);
    }

    @FunctionalInterface
    public interface AfterChop {
        /**
         * Signals that a block has been chopped.
         */
        void afterChop(Level world, Player player, BlockPos pos, BlockState state, ChopDataImmutable chopData, boolean felled);
    }

    @FunctionalInterface
    public interface DetectTree {
        /**
         * Used to determine what tree a block belongs to. This is used to:
         * <ul>
         *   <li> trigger chopping when a choppable block is broken
         *   <li> activate the on-screen chop indicator when the player highlights a choppable block
         *   <li> add tree information to Jade/WTHIT/TheOneProbe/etc. popups
         * </ul>
         * Note that detection events only trigger for blocks that are considered choppable (see {@link TreeChopAPI#isBlockChoppable}).
         *
         * @return null to prevent tree detection
         */
        TreeData onDetectTree(Level level, ServerPlayer player, BlockPos blockPos, BlockState blockState, TreeData treeData);
    }
}
