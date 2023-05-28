package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.api.TreeData;
import ht.treechop.client.Client;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.registry.ForgeModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@WailaPlugin
public class Jade implements IWailaPlugin, IBlockComponentProvider {

    public static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TreeChop.MOD_ID, "show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TreeChop.MOD_ID, "show_num_chops_remaining");
    private static final ResourceLocation UID = TreeChop.resource("plugin");

    @Override
    public void registerClient(IWailaClientRegistration registrar) {
        registrar.registerBlockComponent(this, Block.class);
        registrar.registerBlockIcon(this, ChoppedLogBlock.class);
        registrar.addConfig(SHOW_TREE_BLOCKS, true);
        registrar.addConfig(SHOW_NUM_CHOPS_REMAINING, true);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        changeBlockName(tooltip, accessor);

        if (ChopUtil.playerWantsToChop(accessor.getPlayer(), Client.getChopSettings())
                && ChopUtil.isBlockChoppable(accessor.getLevel(), accessor.getPosition(), accessor.getBlockState())
                && (config.get(SHOW_TREE_BLOCKS) || config.get(SHOW_NUM_CHOPS_REMAINING))) {
            Level level = accessor.getLevel();
            AtomicInteger numChops = new AtomicInteger(0);

            int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();
            TreeData tree = Client.treeCache.getTree(level, accessor.getPosition(), maxNumTreeBlocks);
            if (tree.isAProperTree(Client.getChopSettings().getTreesMustHaveLeaves())) {
                tree.getLogBlocks().ifPresent(
                        treeBlocks -> {
                            if (config.get(SHOW_NUM_CHOPS_REMAINING)) {
                                treeBlocks.forEach((BlockPos pos) -> numChops.getAndAdd(ChopUtil.getNumChops(level, pos)));
                                tooltip.add(Component.translatable("treechop.waila.x_out_of_y_chops", numChops.get(), ChopUtil.numChopsToFell(level, treeBlocks)));
                            }

                            if (config.get(SHOW_TREE_BLOCKS)) {
                                LinkedList<IElement> tiles = new LinkedList<>();
                                treeBlocks.stream()
                                        .collect(Collectors.groupingBy((BlockPos pos) -> {
                                            BlockState state = level.getBlockState(pos);
                                            return WailaUtil.getLogState(level, pos, state).getBlock();
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

    private static void changeBlockName(ITooltip tooltip, BlockAccessor accessor) {
        final ResourceLocation OBJECT_NAME_COMPONENT_KEY = new ResourceLocation("somtin", "somtin");
        try {
            if (accessor.getBlockEntity() instanceof ChoppedLogBlock.MyEntity choppedEntity) {
                String choppedLogName = Language.getInstance().getOrDefault("block." + ForgeModBlocks.CHOPPED_LOG.getKey().location().toLanguageKey());
                List<Component> siblings = tooltip.get(OBJECT_NAME_COMPONENT_KEY).get(0).getMessage().getSiblings();
                for (int i = 0, n = siblings.size(); i < n; ++i) {
                    if (siblings.get(i).getString().matches(choppedLogName)) {
                        siblings.set(i, WailaUtil.getPrefixedBlockName(choppedEntity));
                        break;
                    }
                }
            }
        } catch (NullPointerException ignored) {
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
