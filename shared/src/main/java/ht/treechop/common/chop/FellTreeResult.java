package ht.treechop.common.chop;

import ht.treechop.TreeChop;
import ht.treechop.api.ILeaveslikeBlock;
import ht.treechop.api.TreeData;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.config.FellCreditStrategy;
import ht.treechop.common.config.FellLeavesStrategy;
import ht.treechop.common.util.ClassUtil;
import ht.treechop.common.util.LevelUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class FellTreeResult implements ChopResult {
    private final Level level;
    private final FellDataImpl fellData;
    private final Collection<Chop> chops;

    public FellTreeResult(Level level, TreeData tree, boolean breakLeaves, Collection<Chop> chops) {
        this.level = level;
        this.fellData = new FellDataImpl(tree, breakLeaves);
        this.chops = chops;
    }

    @Override
    public void apply(BlockPos targetPos, ServerPlayer player, ItemStack tool) {
        GameType gameType = player.gameMode.getGameModeForPlayer();

        if (level instanceof ServerLevel serverLevel && !serverLevel.getBlockState(targetPos).isAir() && !player.blockActionRestricted(serverLevel, targetPos, gameType)) {
            boolean fell = TreeChop.platform.startFellTreeEvent(player, level, targetPos, fellData);

            chops.forEach(chop -> chop.apply(level, player, tool, fell));

            if (fell) {
                Consumer<BlockPos> blockBreaker = makeBlockBreaker(player, serverLevel);
                breakLogs(player, serverLevel, fellData.getTree(), gameType, blockBreaker, targetPos);

                if (fellData.getBreakLeaves()) {
                    breakLeaves(player, serverLevel, fellData.getTree(), gameType, blockBreaker);
                }

                TreeChop.platform.finishFellTreeEvent(player, level, targetPos, fellData);
            }
        }
    }

    @NotNull
    private static Consumer<BlockPos> makeBlockBreaker(ServerPlayer player, ServerLevel level) {
        if (player.isCreative()) {
            BlockState air = Blocks.AIR.defaultBlockState();
            return pos -> level.setBlockAndUpdate(pos, air);
        } else {
            ServerPlayer creditPlayer = (ConfigHandler.COMMON.fellCreditStrategy.get() == FellCreditStrategy.NONE) ? null : player;
            ItemStack creditTool = (ConfigHandler.COMMON.fellCreditStrategy.get() == FellCreditStrategy.PLAYER_AND_TOOL) ? player.getMainHandItem() : ItemStack.EMPTY;
            return pos -> LevelUtil.harvestBlock(creditPlayer, level, pos, creditTool, false);
        }
    }

    private static void breakLogs(ServerPlayer player, ServerLevel level, TreeData tree, GameType gameType, Consumer<BlockPos> blockBreaker, BlockPos targetPos) {
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

    private static void breakLeaves(ServerPlayer player, ServerLevel level, TreeData tree, GameType gameType, Consumer<BlockPos> blockBreaker) {
        final long maxNumEffects = 5;
        AtomicInteger i = new AtomicInteger(0);
        PriorityQueue<Pair<BlockPos, BlockState>> effects = new PriorityQueue<>(Comparator.comparing(pair -> pair.getLeft().getY()));

        boolean tryToDecay = ConfigHandler.COMMON.fellLeavesStrategy.get() == FellLeavesStrategy.DECAY;
        Consumer<BlockPos> leavesBreaker = pos -> {
            if (!player.blockActionRestricted(level, pos, gameType)) {
                BlockState state = level.getBlockState(pos);

                ILeaveslikeBlock leavesLike = ClassUtil.getLeaveslikeBlock(state.getBlock());
                if (leavesLike != null) {
                    leavesLike.fell(player, level, pos, state);
                } else if (tryToDecay && shouldDecayLeaves(state)) {
                    decayLeavesInsteadOfBreaking(level, pos, state);
                } else {
                    blockBreaker.accept(pos);
                }

                if (effects.isEmpty() || player.distanceToSqr(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5) > 9.0) {
                    collectSomeBlocks(effects, pos, state, i, 8);
                }
            }
        };

        tree.forEachLeaves(leavesBreaker);

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

    private static void decayLeavesInsteadOfBreaking(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState decayingState = state.setValue(LeavesBlock.PERSISTENT, false).setValue(LeavesBlock.DISTANCE, LeavesBlock.DECAY_DISTANCE);
        decayingState.randomTick(level, pos, level.random);
    }

    private static boolean shouldDecayLeaves(BlockState blockState) {
        return blockState.hasProperty(LeavesBlock.DISTANCE) && blockState.hasProperty(LeavesBlock.PERSISTENT)
                && blockState.setValue(LeavesBlock.DISTANCE, 7).setValue(LeavesBlock.PERSISTENT, false).isRandomlyTicking(); // Catches modded leaves that don't decay
    }
}
