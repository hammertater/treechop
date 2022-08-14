package ht.treechop.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.atomic.AtomicBoolean;

public final class TreeChopEvents {
    private TreeChopEvents() { }

    public static final Event<BeforeChop> BEFORE_CHOP = EventFactory.createArrayBacked(BeforeChop.class,
            (listeners) -> (world, player, pos, state, chopData) -> {
                for (BeforeChop event : listeners) {
                    boolean result = event.beforeChop(world, player, pos, state, chopData);

                    if (!result) {
                        return false;
                    }
                }

                return true;
            }
    );

    public static final Event<AfterChop> AFTER_CHOP = EventFactory.createArrayBacked(AfterChop.class,
            (listeners) -> (world, player, pos, state) -> {
                for (AfterChop event : listeners) {
                    event.afterChop(world, player, pos, state);
                }
            }
    );

    public static final Event<DetectTree> DETECT_TREE = EventFactory.createArrayBacked(DetectTree.class,
            (listeners) -> (world, player, pos, state, hasLeaves, overrideHasLeaves) -> {
                for (DetectTree event : listeners) {
                    event.onDetectTree(world, player, pos, state, hasLeaves, overrideHasLeaves);
                }
            }
    );

    @FunctionalInterface
    public interface BeforeChop {
        /**
         *
         * @param world
         * @param player
         * @param pos
         * @param state
         * @param chopData Stores the number of chops to perform and whether to fell if possible.
         * @return {@code false} to prevent chopping and instead break the block normally.
         */
        boolean beforeChop(Level world, Player player, BlockPos pos, BlockState state, ChopData chopData);
    }

    @FunctionalInterface
    public interface AfterChop {
        void afterChop(Level world, Player player, BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface DetectTree {
        void onDetectTree(Level world, Player player, BlockPos pos, BlockState state, AtomicBoolean hasLeaves, AtomicBoolean overrideHasLeaves);
    }
}
