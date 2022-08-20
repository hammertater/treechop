package ht.treechop.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class TreeChopEvents {
    private TreeChopEvents() { }

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
            (listeners) -> (world, player, pos, state) -> {
                for (AfterChop event : listeners) {
                    event.afterChop(world, player, pos, state);
                }
            }
    );

    public static final Event<DetectTree> DETECT_TREE = EventFactory.createArrayBacked(DetectTree.class,
            (listeners) -> (world, player, pos, state, overrideHasLeaves) -> {
                for (DetectTree listener : listeners) {
                    if (!listener.onDetectTree(world, player, pos, state, overrideHasLeaves)) {
                        return false;
                    }
                }
                return true;
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
        boolean onDetectTree(Level level, ServerPlayer player, BlockPos blockPos, BlockState blockState, boolean overrideLeaves);
    }
}
