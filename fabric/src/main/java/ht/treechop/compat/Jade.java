package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.ItemStackElement;

import java.util.LinkedList;
import java.util.List;

@WailaPlugin
public class Jade implements IWailaPlugin, IBlockComponentProvider {

    public static final ResourceLocation SHOW_TREE_BLOCKS = ResourceLocation.fromNamespaceAndPath(TreeChop.MOD_ID, "show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = ResourceLocation.fromNamespaceAndPath(TreeChop.MOD_ID, "show_num_chops_remaining");
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

        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        boolean showNumBlocks = config.get(SHOW_TREE_BLOCKS);
        boolean showChopsRemaining = config.get(SHOW_NUM_CHOPS_REMAINING);

        if (WailaUtil.playerWantsTreeInfo(level, pos, showNumBlocks, showChopsRemaining)) {
            List<IElement> tiles = new LinkedList<>();
            WailaUtil.addTreeInfo(
                    level,
                    pos,
                    showNumBlocks,
                    showChopsRemaining,
                    tooltip::add,
                    stack -> {
                        IElement icon = IElementHelper.get().item(stack, 1f, Integer.toString(stack.getCount()));
                        tiles.add(icon.translate(new Vec2(0, -1.5f)));
                    }
            );
            tooltip.add(tiles);
        }
    }

    private static void changeBlockName(ITooltip tooltip, BlockAccessor accessor) {
        final ResourceLocation OBJECT_NAME_COMPONENT_KEY = ResourceLocation.fromNamespaceAndPath("jade", "object_name");
        if (accessor.getBlockEntity() instanceof ChoppedLogBlock.MyEntity choppedEntity) {
            // There's no API function to change the message, so let's replace it
            tooltip.clear();

            Component newName = WailaUtil.getPrefixedBlockName(choppedEntity);
            IElement newNameElement = IElementHelper.get()
                    .text(IThemeHelper.get().title(newName))
                    .tag(OBJECT_NAME_COMPONENT_KEY);

            tooltip.add(newNameElement);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
