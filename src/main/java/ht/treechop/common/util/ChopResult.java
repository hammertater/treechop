package ht.treechop.common.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayerFactory;

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

    private final World world;
    private final Collection<Chop> chops;
    private final Collection<BlockPos> fells;
    private final boolean felling;

    public static final int MAX_NUM_FELLING_EFFECTS = 32;;

    public ChopResult(World world, Collection<Chop> chops, Collection<BlockPos> fells) {
        this.world = world;
        this.chops = chops;
        this.fells = fells;
        this.felling = !fells.isEmpty();
    }

    /**
     *  Applies the results of chopping to the world, spawning the appropriate drops.
     * - Chopped blocks: harvest by agent, change to chopped state
     * - Felled blocks: harvest by no one, change to felled state
     * - Chopped and felled blocks: harvest by agent, change to felled state
     * @return whether the block at targetPos needs to be preserved.
     */
    public boolean apply(BlockPos targetPos, ServerPlayerEntity agent, ItemStack tool, boolean breakLeaves) {
        GameType gameType;
        gameType = agent.gameMode.getGameModeForPlayer();

        AtomicBoolean somethingChanged = new AtomicBoolean(false);
        List<BlockPos> logs = Stream.concat(chops.stream().map(Chop::getBlockPos), fells.stream())
                .filter(pos -> !somethingChanged.get() && ChopUtil.canChangeBlock(world, pos, agent, gameType, tool))
                .peek(pos -> {
                    BlockState blockState = world.getBlockState(pos);
                    somethingChanged.compareAndSet(false, blockState.isAir());
                })
                .collect(Collectors.toList());

        if (somethingChanged.get()) {
            return false;
        }

        chopBlocks(world, agent, tool, chops.stream());

        if (felling) {
            List<BlockPos> leaves = breakLeaves
                    ? ChopUtil.getTreeLeaves(world, logs).stream()
                    .filter(pos -> ChopUtil.canChangeBlock(world, pos, agent, agent.gameMode.getGameModeForPlayer()))
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            logs.remove(targetPos);
            playBlockBreakEffects(world, logs, leaves);

            fells.remove(targetPos);
            fellBlocks(world, targetPos, agent, Stream.of(fells, leaves).flatMap(Collection::stream));

            return false;
        }

        return true;
    }

    private void chopBlocks(World world, PlayerEntity player, ItemStack tool, Stream<Chop> chops) {
        chops.forEach(chop -> chop.apply(world, player, tool, felling));
    }

    private void fellBlocks(World world, BlockPos targetPos, ServerPlayerEntity agent, Stream<BlockPos> blocks) {
        PlayerEntity fakePlayer = (world instanceof ServerWorld)
                ? FakePlayerFactory.getMinecraft((ServerWorld) world)
                : agent;

        AtomicInteger xpAccumulator = new AtomicInteger(0);
        Consumer<BlockPos> blockBreaker;

        if (world.isClientSide() || agent.isCreative()) {
            BlockState air = Blocks.AIR.defaultBlockState();
            blockBreaker = pos -> world.setBlock(pos, air, 3);
        } else {
            blockBreaker = pos -> harvestWorldBlock(fakePlayer, world, pos, ItemStack.EMPTY, xpAccumulator, 0, 0);
        }

        blocks.forEach(blockBreaker);
        ChopUtil.dropExperience(world, targetPos, xpAccumulator.get());
    }

    private void playBlockBreakEffects(World world, List<BlockPos> logs, List<BlockPos> leaves) {
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
                .forEach(pos -> world.levelEvent(2001, pos, Block.getId(world.getBlockState(pos))));
    }

    private static void harvestWorldBlock(
            PlayerEntity agent,
            World world,
            BlockPos pos,
            ItemStack tool,
            AtomicInteger totalXp,
            int fortune,
            int silkTouch
    ) {
        BlockState blockState = world.getBlockState(pos);

        // Do not call -- makes particle and sound effects
        // blockState.removedByPlayer(world, pos, agent, true, world.getFluidState(pos));

        if (world instanceof ServerWorld) {
            FluidState fluidStateOrAir = world.getFluidState(pos);
            blockState.getBlock().destroy(world, pos, blockState);
            Block.dropResources(blockState, world, pos, world.getBlockEntity(pos), agent, tool);
            totalXp.getAndAdd(blockState.getExpDrop(world, pos, fortune, silkTouch));
            world.setBlock(pos, fluidStateOrAir.createLegacyBlock(), 3);
        }
    }

    public boolean isFelling() {
        return fells.size() > 0;
    }
}
