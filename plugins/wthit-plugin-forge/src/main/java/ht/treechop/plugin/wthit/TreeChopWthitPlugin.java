package ht.treechop.plugin.wthit;

import ht.treechop.TreeChop;
import ht.treechop.api.TreeData;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import mcp.mobius.waila.api.component.PairComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@WailaPlugin(id = TreeChop.MOD_ID)
@Mod(TreeChopWthitPlugin.MOD_ID)
public class TreeChopWthitPlugin implements IWailaPlugin, IBlockComponentProvider {
    public static final String MOD_ID = "treechopwthit";
    public static final String TREECHOP_ID = "treechop";
    private static final TreeChopWthitPlugin INSTANCE = new TreeChopWthitPlugin();

    public static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TREECHOP_ID, "show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TREECHOP_ID, "show_num_chops_remaining");

    @Override
    public void register(IRegistrar registrar) {
        registrar.addConfig(SHOW_TREE_BLOCKS, false);
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
                                tooltip.addLine(new WrappedComponent(new TranslatableComponent("treechop.waila.x_out_of_y_chops", numChops.get(), ChopUtil.numChopsToFell(treeBlocks.size()))));
                            }

                            if (config.getBoolean(SHOW_TREE_BLOCKS)) {
                                LinkedList<ITooltipComponent> tiles = new LinkedList<>();
                                treeBlocks.stream()
                                        .collect(Collectors.groupingBy((BlockPos pos) -> {
                                            BlockState state = level.getBlockState(pos);
                                            return getLogState(level, pos, state).getBlock();
                                        }, Collectors.counting()))
                                        .forEach((block, count) -> {
                                            ItemStack stack = accessor.getBlock().asItem().getDefaultInstance();
                                            stack.setCount(count.intValue());
                                            tiles.add(new ItemComponent(stack));
                                        });

                                tiles.stream().reduce(PairComponent::new).ifPresent(tooltip::addLine);
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