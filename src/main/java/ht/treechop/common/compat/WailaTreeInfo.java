package ht.treechop.common.compat;

import ht.treechop.TreeChopMod;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.util.ChopUtil;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.__internal__.IApiService;
import mcp.mobius.waila.api.component.ItemComponent;
import mcp.mobius.waila.api.component.PairComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public enum WailaTreeInfo implements IBlockComponentProvider {
    INSTANCE;

    public static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TreeChopMod.MOD_ID, "show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TreeChopMod.MOD_ID, "show_num_chops_remaining");

    @Override
    public @Nullable BlockState getOverride(IBlockAccessor accessor, IPluginConfig config) {
        return getLogState(accessor.getWorld(), accessor.getPosition(), accessor.getBlockState());
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (ChopUtil.playerWantsToChop(accessor.getPlayer())
                && ChopUtil.isBlockChoppable(accessor.getWorld(), accessor.getPosition(), accessor.getBlockState())
                && (config.getBoolean(SHOW_TREE_BLOCKS) || config.getBoolean(SHOW_NUM_CHOPS_REMAINING))) {
            Level level = accessor.getWorld();
            AtomicInteger numChops = new AtomicInteger(0);

            Set<BlockPos> treeBlocks = ChopUtil.getTreeBlocks(level, accessor.getPosition(), Client.getChopSettings().getTreesMustHaveLeaves());

            if (config.getBoolean(SHOW_TREE_BLOCKS)) {
                treeBlocks.stream()
                        .collect(Collectors.groupingBy((BlockPos pos) -> {
                            BlockState state = level.getBlockState(pos);
                            numChops.getAndAdd(ChopUtil.getNumChops(level, pos, state));
                            return getLogState(level, pos, state).getBlock();
                        }, Collectors.counting()))
                        .forEach((block, count) -> {
                            ITooltipComponent tooltipIcon = new ItemComponent(accessor.getBlock().asItem());
                            ITooltipComponent tooltipCount = new WrappedComponent(new TextComponent(count.toString()));
                            tooltip.addLine(new PairComponent(tooltipIcon, tooltipCount));
                        });
            } else {
                treeBlocks.forEach((BlockPos pos) -> numChops.getAndAdd(ChopUtil.getNumChops(level, pos)));
            }

            if (config.getBoolean(SHOW_NUM_CHOPS_REMAINING)) {
                tooltip.addLine(new WrappedComponent(new TextComponent(String.format("%d/%d chops", numChops.get(), ChopUtil.numChopsToFell(treeBlocks.size())))));
            }
        }
    }

    private BlockState getLogState(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.Entity entity) {
            return entity.getOriginalState();
        } else {
            return state;
        }
    }
}
