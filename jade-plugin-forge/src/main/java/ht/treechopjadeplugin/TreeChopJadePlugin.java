package ht.treechopjadeplugin;

import ht.treechop.api.TreeData;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.util.ChopUtil;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElement;
import mcp.mobius.waila.impl.ui.ItemStackElement;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Mod(TreeChopJadePlugin.MOD_ID)
@WailaPlugin
public class TreeChopJadePlugin implements IWailaPlugin, IComponentProvider {
    public static final String MOD_ID = "treechopjadeplugin";
    public static final String TREECHOP_ID = "treechop";
    private static final TreeChopJadePlugin INSTANCE = new TreeChopJadePlugin();
    private static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TREECHOP_ID, "show_tree_block_counts");
    private static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TREECHOP_ID, "show_num_chops_remaining");

    public TreeChopJadePlugin() {
    }

    @Override
    public void register(IWailaCommonRegistration registrar) {
        registrar.addConfig(SHOW_TREE_BLOCKS, true);
        registrar.addConfig(SHOW_NUM_CHOPS_REMAINING, true);
    }

    @Override
    public void registerClient(IWailaClientRegistration registrar) {
        registrar.registerComponentProvider(INSTANCE, TooltipPosition.BODY, Block.class);
    }

    @Override
    public @Nullable IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
        BlockState state = getLogState(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState());
        return ItemStackElement.of(
                (state != accessor.getBlockState())
                        ? state.getBlock().asItem().getDefaultInstance()
                        : Items.OAK_LOG.getDefaultInstance()
        );
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (ChopUtil.playerWantsToChop(accessor.getPlayer(), Client.getChopSettings())
                && ChopUtil.isBlockChoppable(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState())
                && (config.get(SHOW_TREE_BLOCKS) || config.get(SHOW_NUM_CHOPS_REMAINING))) {
            Level level = accessor.getLevel();
            AtomicInteger numChops = new AtomicInteger(0);

            TreeData tree = Client.treeCache.getTree(level, accessor.getPosition());
            if (tree.isAProperTree(Client.getChopSettings().getTreesMustHaveLeaves())) {
                tree.getLogBlocks().ifPresent(
                        treeBlocks -> {
                            if (config.get(SHOW_NUM_CHOPS_REMAINING)) {
                                treeBlocks.forEach((BlockPos pos) -> numChops.getAndAdd(ChopUtil.getNumChops(level, pos)));
                                tooltip.add(new TranslatableComponent("treechop.waila.x_out_of_y_chops", numChops.get(), ChopUtil.numChopsToFell(treeBlocks.size())));
                            }

                            if (config.get(SHOW_TREE_BLOCKS)) {
                                LinkedList<IElement> tiles = new LinkedList<>();
                                treeBlocks.stream()
                                        .collect(Collectors.groupingBy((BlockPos pos) -> {
                                            BlockState state = level.getBlockState(pos);
                                            return getLogState(level, pos, state).getBlock();
                                        }, Collectors.counting()))
                                        .forEach((block, count) -> {
                                            IElement icon = tooltip.getElementHelper().item(block.asItem().getDefaultInstance(), 1f, count.toString());
                                            tiles.add(icon.translate(new Vec2(0, -1.5f)));
                                        });
                                tooltip.add(tiles);
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