package ht.treechop.common.chop;

import ht.treechop.api.TreeData;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.util.BlockNeighbors;
import ht.tuber.graph.DirectedGraph;
import ht.tuber.graph.FloodFillImpl;
import ht.tuber.graph.GraphUtil;
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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;

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
        tree.streamLogs().filter(pos -> !pos.equals(targetPos) && !player.blockActionRestricted(level, targetPos, gameType))
                .forEach(blockBreaker);
    }

    private void breakLeaves(ServerPlayer player, ServerLevel level, GameType gameType, Consumer<BlockPos> blockBreaker) {
        AtomicInteger distance = new AtomicInteger();

        DirectedGraph<BlockPos> leavesGraph = GraphUtil.filterNeighbors(
                BlockNeighbors.ADJACENTS::asStream,
                pos -> {
                    BlockState state = level.getBlockState(pos);
                    return ChopUtil.isBlockLeaves(state) && (leavesHasAtLeastDistance(state, distance.get()));
                }
        );

        Consumer<BlockPos> leavesBreaker = pos -> {
            if (!player.blockActionRestricted(level, pos, gameType)) {
                BlockState state = level.getBlockState(pos);
                if (isVanillaLeaves(state)) {
                    decayLeavesInsteadOfBreaking(level, pos, state);
                } else {
                    blockBreaker.accept(pos);
                }
            }
        };

        List<BlockPos> leaves = tree.streamLeaves().filter(pos -> leavesHasExactDistance(level.getBlockState(pos), 1)).toList();
        leaves.forEach(leavesBreaker);

//        leaves = GraphUtil.flood(leavesGraph, leaves).fill().peek(leavesBreaker).toList();

        FloodFillImpl<BlockPos> flood = new FloodFillImpl<>(leaves, leavesGraph, a -> 0);

        int maxDistance = ConfigHandler.COMMON.maxBreakLeavesDistance.get();
        for (int i = 2; i < maxDistance; ++i) {
            distance.set(i);
            flood.fillOnce(leavesBreaker);
        }
    }

    private static void playBlockBreakEffects(Level level, List<BlockPos> logs, List<BlockPos> leaves) {
        int numLogsAndLeaves = logs.size() + leaves.size();
        int numEffects = Math.min((int) Math.ceil(Math.sqrt(numLogsAndLeaves)), MAX_NUM_FELLING_EFFECTS) - 1;
        int numLeavesEffects = Math.max(1, (int) Math.ceil(numEffects * ((double) leaves.size() / (double) numLogsAndLeaves)));
        int numLogsEffects = Math.max(1, numEffects - numLeavesEffects);

        Collections.shuffle(logs);
        Collections.shuffle(leaves);

        Stream.concat(
                        logs.stream().limit(numLogsEffects),
                        leaves.stream().limit(numLeavesEffects)
                )
                .forEach(pos -> level.levelEvent(2001, pos, Block.getId(level.getBlockState(pos))));
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

    private boolean leavesHasExactDistance(BlockState state, int distance) {
        return state.hasProperty(LeavesBlock.DISTANCE)
                ? state.getValue(LeavesBlock.DISTANCE) == distance
                : true;
    }

    private boolean leavesHasAtLeastDistance(BlockState state, int distance) {
        return state.hasProperty(LeavesBlock.DISTANCE)
                ? state.getValue(LeavesBlock.DISTANCE) >= distance
                : true;
    }

    private static boolean isVanillaLeaves(BlockState blockState) {
        return blockState.hasProperty(LeavesBlock.DISTANCE);
    }
}
