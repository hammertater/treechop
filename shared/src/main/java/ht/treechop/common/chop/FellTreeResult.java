package ht.treechop.common.chop;

import ht.treechop.api.TreeData;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FellTreeResult implements ChopResult {
    public static final int MAX_NUM_FELLING_EFFECTS = 32;

    private final Level level;
    private final TreeData tree;

    public FellTreeResult(Level level, TreeData tree) {
        this.level = level;
        this.tree = tree;
    }

    @Override
    public void apply(BlockPos targetPos, ServerPlayer player, ItemStack tool, boolean breakLeaves) {
        GameType gameType = player.gameMode.getGameModeForPlayer();

        if (level instanceof ServerLevel serverLevel && !serverLevel.getBlockState(targetPos).isAir() && !player.blockActionRestricted(serverLevel, targetPos, gameType)) {
            Consumer<BlockPos> blockBreaker = makeBlockBreaker(player, serverLevel);
            breakLogs(player, serverLevel, gameType, blockBreaker, targetPos);

            if (breakLeaves) {
                breakLeaves(player, serverLevel, gameType, blockBreaker);
            }
        }
    }

    @NotNull
    private static Consumer<BlockPos> makeBlockBreaker(ServerPlayer player, ServerLevel level) {
        if (player.isCreative()) {
            BlockState air = Blocks.AIR.defaultBlockState();
            return pos -> level.setBlockAndUpdate(pos, air);
        } else {
            return pos -> harvestWorldBlock(null, level, pos, ItemStack.EMPTY);
        }
    }

    private void breakLogs(ServerPlayer player, ServerLevel level, GameType gameType, Consumer<BlockPos> blockBreaker, BlockPos targetPos) {
        final long maxNumEffects = 4;
        AtomicInteger i = new AtomicInteger(0);
        PriorityQueue<Pair<BlockPos, BlockState>> effects = new PriorityQueue<>(Comparator.comparing(pair -> pair.getLeft().getY()));

        tree.streamLogs()
                .filter(pos -> !pos.equals(targetPos) && !player.blockActionRestricted(level, targetPos, gameType))
                .forEach(pos -> {
                    collectSomeBlocks(effects, pos, level.getBlockState(pos), i, 3);
                    blockBreaker.accept(pos);
                });

        effects.stream()
                .limit(maxNumEffects)
                .forEach(posState -> playBlockBreakEffects(level, posState.getLeft(), posState.getRight()));
    }

    private void breakLeaves(ServerPlayer player, ServerLevel level, GameType gameType, Consumer<BlockPos> blockBreaker) {
        final long maxNumEffects = 5;
        AtomicInteger i = new AtomicInteger(0);
        PriorityQueue<Pair<BlockPos, BlockState>> effects = new PriorityQueue<>(Comparator.comparing(pair -> pair.getLeft().getY()));

        Consumer<BlockPos> leavesBreaker = pos -> {
            if (!player.blockActionRestricted(level, pos, gameType)) {
                BlockState state = level.getBlockState(pos);
                if (isVanillaLeaves(state)) {
                    decayLeavesInsteadOfBreaking(level, pos, state);
                } else {
                    blockBreaker.accept(pos);
                }

                if (player.distanceToSqr(pos.getCenter()) > 9.0) {
                    collectSomeBlocks(effects, pos, state, i, 8);
                }
            }
        };

        tree.streamLeaves().forEach(leavesBreaker);

        effects.stream()
                .limit(maxNumEffects)
                .forEach(posState -> playBlockBreakEffects(level, posState.getLeft(), posState.getRight()));
    }

    private static void collectSomeBlocks(Queue<Pair<BlockPos, BlockState>> collection, BlockPos pos, BlockState state, AtomicInteger counter, int period) {
        if (counter.getAndIncrement() % period == 0) {
            collection.add(Pair.of(pos, state));
        }
    }

    private static void playBlockBreakEffects(Level level, BlockPos pos, BlockState state) {
        level.levelEvent(2001, pos, Block.getId(state));
    }

    private static void harvestWorldBlock(
            Entity agent,
            Level level,
            BlockPos pos,
            ItemStack tool
    ) {
        BlockState blockState = level.getBlockState(pos);

        // Do not call -- makes particle and sound effects
        // blockState.removedByPlayer(level, pos, agent, true, level.getFluidState(pos));

        if (level instanceof ServerLevel) {
            FluidState fluidStateOrAir = level.getFluidState(pos);
            blockState.getBlock().destroy(level, pos, blockState);
            Block.dropResources(blockState, level, pos, level.getBlockEntity(pos), agent, tool); // Should drop XP
            level.setBlockAndUpdate(pos, fluidStateOrAir.createLegacyBlock());
        }
    }

    private void decayLeavesInsteadOfBreaking(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState decayingState = state.setValue(LeavesBlock.PERSISTENT, false).setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE);
        decayingState.randomTick(level, pos, level.random);
    }

    private static boolean isVanillaLeaves(BlockState blockState) {
        return blockState.hasProperty(LeavesBlock.DISTANCE);
    }
}
