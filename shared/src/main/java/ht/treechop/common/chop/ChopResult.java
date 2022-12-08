package ht.treechop.common.chop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopResult {
    public static final ChopResult IGNORED = new ChopResult(null, Collections.emptyList(), Collections.emptyList());
    public static final int MAX_NUM_FELLING_EFFECTS = 32;
    private final Level level;
    private final Collection<Chop> chops;
    private final Collection<BlockPos> fells;

    public ChopResult(Level level, Collection<Chop> chops, Collection<BlockPos> fells) {
        this.level = level;
        this.chops = chops;
        this.fells = fells;
    }

    private static void chopBlocks(Level level, Player player, ItemStack tool, Stream<Chop> chops, boolean felling) {
        chops.forEach(chop -> chop.apply(level, player, tool, felling));
    }

    private static void fellBlocks(Level level, BlockPos targetPos, Entity agent, Stream<BlockPos> blocks) {
        AtomicInteger xpAccumulator = new AtomicInteger(0);
        Consumer<BlockPos> blockBreaker;

        if (level.isClientSide() || (agent instanceof ServerPlayer player && player.isCreative())) {
            BlockState air = Blocks.AIR.defaultBlockState();
            blockBreaker = pos -> level.setBlockAndUpdate(pos, air);
        } else {
            blockBreaker = pos -> harvestWorldBlock(null, level, pos, ItemStack.EMPTY);
        }

        blocks.forEach(blockBreaker);
        ChopUtil.dropExperience(level, targetPos, xpAccumulator.get());
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

    /**
     * Applies the results of chopping to the level, spawning the appropriate drops. - Chopped blocks: harvest by agent,
     * change to chopped state - Felled blocks: harvest by no one, change to felled state - Chopped and felled blocks:
     * harvest by agent, change to felled state
     *
     * @return whether the block at targetPos needs to be preserved.
     */
    public boolean apply(BlockPos targetPos, ServerPlayer agent, ItemStack tool, boolean breakLeaves) {
        GameType gameType;
        gameType = agent.gameMode.getGameModeForPlayer();

        AtomicBoolean somethingChanged = new AtomicBoolean(false);
        List<BlockPos> logs = Stream.concat(chops.stream().map(Chop::getBlockPos), fells.stream())
                .filter(pos -> !somethingChanged.get() && !agent.blockActionRestricted(level, pos, gameType))
                .peek(pos -> {
                    BlockState blockState = level.getBlockState(pos);
                    somethingChanged.compareAndSet(false, blockState.isAir());
                })
                .collect(Collectors.toList());

        if (somethingChanged.get()) {
            return false;
        }

        chopBlocks(level, agent, tool, chops.stream(), isFelling());

        if (isFelling()) {
            List<BlockPos> leaves = breakLeaves
                    ? ChopUtil.getTreeLeaves(level, logs).stream()
                    .filter(pos -> !agent.blockActionRestricted(level, pos, agent.gameMode.getGameModeForPlayer()))
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            logs.remove(targetPos);
            playBlockBreakEffects(level, logs, leaves);

            fells.remove(targetPos);
            fellBlocks(level, targetPos, agent, Stream.of(fells, leaves).flatMap(Collection::stream));

            return false;
        }

        return true;
    }

    public boolean isFelling() {
        return !fells.isEmpty();
    }
}
