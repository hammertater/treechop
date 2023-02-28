package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.TreeData;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Wthit implements IWailaPlugin, IBlockComponentProvider {
    public static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TreeChop.MOD_ID, "show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TreeChop.MOD_ID, "show_num_chops_remaining");
    private static final Wthit INSTANCE = new Wthit();

    @Override
    public void register(IRegistrar registrar) {
        registrar.addConfig(SHOW_TREE_BLOCKS, true);
        registrar.addConfig(SHOW_NUM_CHOPS_REMAINING, true);
        registrar.addComponent(INSTANCE, TooltipPosition.BODY, Block.class);
        registrar.addOverride(INSTANCE, ChoppedLogBlock.class);
    }

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

            int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();
            TreeData tree = Client.treeCache.getTree(level, accessor.getPosition(), maxNumTreeBlocks);
            if (tree.isAProperTree(Client.getChopSettings().getTreesMustHaveLeaves())) {
                tree.getLogBlocks().ifPresent(
                        treeBlocks -> {
                            if (config.getBoolean(SHOW_NUM_CHOPS_REMAINING)) {
                                treeBlocks.forEach((BlockPos pos) -> numChops.getAndAdd(ChopUtil.getNumChops(level, pos)));
                                tooltip.addLine(new WrappedComponent(Component.translatable("treechop.waila.x_out_of_y_chops", numChops.get(), ChopUtil.numChopsToFell(level, treeBlocks))));
                            }

                            if (config.getBoolean(SHOW_TREE_BLOCKS)) {
                                ITooltipLine line = tooltip.addLine();
                                treeBlocks.stream()
                                        .collect(Collectors.groupingBy((BlockPos pos) -> {
                                            BlockState state = level.getBlockState(pos);
                                            return getLogState(level, pos, state).getBlock();
                                        }, Collectors.counting()))
                                        .forEach((block, count) -> {
                                            ItemStack stack = block.asItem().getDefaultInstance();
                                            stack.setCount(count.intValue());
                                            line.with(new ItemComponent(stack));
                                        });
                            }
                        });
            }
        }
    }

    private BlockState getLogState(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ChoppedLogBlock.MyEntity entity) {
            return entity.getOriginalState();
        } else {
            return state;
        }
    }
}
