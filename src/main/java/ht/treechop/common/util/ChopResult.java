package ht.treechop.common.util;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChopResult {
    public static final ChopResult IGNORED = new ChopResult(Lists.newArrayList(), false);

    private final List<TreeBlock> blocks;
    private final boolean felling;

    public static final int MAX_NUM_FELLING_EFFECTS = 32;

    public ChopResult(List<TreeBlock> blocks, boolean felling) {
        this.blocks = blocks;
        this.felling = felling;
    }

    public ChopResult(List<TreeBlock> blocks) {
        this(blocks, false);
    }

    public ChopResult(Level level, Collection<BlockPos> chopPositions, Collection<BlockPos> fellPositions) {
        this(
                Stream.of(chopPositions, fellPositions)
                        .flatMap(Collection::stream)
                        .map(pos -> new TreeBlock(level, pos, Blocks.AIR.defaultBlockState()))
                        .collect(Collectors.toList()),
                true
        );
    }

    /**
     *  Applies the results of chopping to the level, spawning the appropriate drops.
     * - Chopped blocks: harvest by agent, change to chopped state
     * - Felled blocks: harvest by no one, change to felled state
     * - Chopped and felled blocks: harvest by agent, change to felled state
     * @return true if changes were able to be applied
     */
    public boolean apply(BlockPos targetPos, ServerPlayer agent, ItemStack tool, boolean breakLeaves) {
        Level level = agent.level;

        GameType gameType;
        gameType = agent.gameMode.getGameModeForPlayer();

        AtomicBoolean somethingChanged = new AtomicBoolean(false);
        List<TreeBlock> logs = blocks.stream()
                .filter(treeBlock -> !somethingChanged.get() && ChopUtil.canChangeBlock(
                        treeBlock.getWorld(),
                        treeBlock.getPos(),
                        agent,
                        gameType,
                        (treeBlock.wasChopped()) ? tool : ItemStack.EMPTY

                ))
                .peek(treeBlock -> {
                    BlockState blockState = level.getBlockState(treeBlock.getPos());
                    somethingChanged.compareAndSet(false, blockState.isAir());
                })
                .collect(Collectors.toList());

        if (somethingChanged.get()) {
            return false;
        }

        List<TreeBlock> leaves = (felling && breakLeaves)
                ? ChopUtil.getTreeLeaves(
                                level,
                                logs.stream().map(TreeBlock::getPos).collect(Collectors.toList())
                        )
                        .stream()
                        .filter(pos -> ChopUtil.canChangeBlock(level, pos, agent, agent.gameMode.getGameModeForPlayer()))
                        .map(pos -> new TreeBlock(level, pos, Blocks.AIR.defaultBlockState()))
                        .collect(Collectors.toList())
                : Lists.newArrayList();

        int numLogsAndLeaves = logs.size() + leaves.size();

        if (!level.isClientSide() && !agent.isCreative()) {
            int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
            int silkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool);

            AtomicInteger xpAccumulator = new AtomicInteger(0);

            Player fakePlayer = (level instanceof ServerLevel)
                    ? FakePlayerFactory.getMinecraft((ServerLevel) level)
                    : agent;

            Stream.of(logs, leaves)
                    .flatMap(Collection::stream)
                    .forEach(treeBlock -> {
                        if (treeBlock.wasChopped()) {
                            harvestWorldBlock(agent, tool, xpAccumulator, fortune, silkTouch, treeBlock);
                        } else {
                            harvestWorldBlock(fakePlayer, ItemStack.EMPTY, xpAccumulator, 0, 0, treeBlock);
                        }
                    });

            ChopUtil.dropExperience(level, targetPos, xpAccumulator.get());
        }

        int numEffects = Math.min((int) Math.ceil(Math.sqrt(numLogsAndLeaves)), MAX_NUM_FELLING_EFFECTS) - 1;

        Collections.shuffle(logs);
        Collections.shuffle(leaves);
        int numLeavesEffects = Math.max(0, (int) Math.ceil(numEffects * ((double) leaves.size() / (double) numLogsAndLeaves)));
        int numLogsEffects = Math.max(0, numEffects - numLeavesEffects);

        Stream.of(
                logs.stream().limit(numLogsEffects),
                leaves.stream().limit(numLeavesEffects)
        )
                .flatMap(a->a)
                .forEach(
                        treeBlock -> level.levelEvent(
                                2001,
                                treeBlock.getPos(),
                                Block.getId(level.getBlockState(treeBlock.getPos()))
                        )
                );

        Stream.of(logs, leaves)
                .flatMap(Collection::stream)
                .forEach(
                        treeBlock -> treeBlock.getWorld().setBlock(
                                treeBlock.getPos(),
                                treeBlock.getState(),
                                3
                        )
                );

        return true;
    }

    private static void harvestWorldBlock(
            Player agent,
            ItemStack tool,
            AtomicInteger totalXp,
            int fortune,
            int silkTouch,
            TreeBlock treeBlock
    ) {
        Level level = treeBlock.getWorld();
        BlockPos pos = treeBlock.getPos();
        BlockState blockState = level.getBlockState(pos);
        boolean destroyed = blockState.removedByPlayer(
                 level, pos, agent, true, level.getFluidState(pos)
        );

        if (destroyed) {
            blockState.getBlock().destroy(level, pos, blockState);
            Block.dropResources(blockState, level, pos, level.getBlockEntity(pos), agent, tool);
            totalXp.getAndAdd(blockState.getExpDrop(level, pos, fortune, silkTouch));
        }
    }

}
