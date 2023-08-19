package ht.treechop.compat;

import ht.treechop.TreeChop;
import ht.treechop.client.Client;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.block.ChoppedLogBlock;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class Wthit implements IWailaPlugin, IBlockComponentProvider {
    public static final ResourceLocation SHOW_TREE_BLOCKS = new ResourceLocation(TreeChop.MOD_ID, "show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = new ResourceLocation(TreeChop.MOD_ID, "show_num_chops_remaining");
    private static final Wthit INSTANCE = new Wthit();

    @Override
    public void register(IRegistrar registrar) {
        registrar.addConfig(SHOW_TREE_BLOCKS, true);
        registrar.addConfig(SHOW_NUM_CHOPS_REMAINING, true);
        registrar.addComponent(INSTANCE, TooltipPosition.BODY, Block.class);
        registrar.addComponent(INSTANCE, TooltipPosition.HEAD, ChoppedLogBlock.class);
    }

    @Override
    public void appendHead(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof ChoppedLogBlock.MyEntity entity) {
            changeBlockName(tooltip, entity);
        }
    }

    private static void changeBlockName(ITooltip tooltip, ChoppedLogBlock.MyEntity entity) {
        String prefixedBlockName = WailaUtil.getPrefixedBlockName(entity).getString();
        Component newNameComponent = IWailaConfig.get().getFormatter().blockName(prefixedBlockName);
        tooltip.setLine(WailaConstants.OBJECT_NAME_TAG, newNameComponent);
    }

    @Override
    public void appendBody(ITooltip tooltip, IBlockAccessor accessor, IPluginConfig config) {
        Level level = accessor.getWorld();
        BlockPos pos = accessor.getPosition();
        boolean showNumBlocks = config.getBoolean(SHOW_TREE_BLOCKS);
        boolean showChopsRemaining = config.getBoolean(SHOW_NUM_CHOPS_REMAINING);

        if (WailaUtil.playerWantsTreeInfo(level, pos, showNumBlocks, showChopsRemaining)) {
            Optional<ITooltipLine> line = Optional.empty();
            WailaUtil.addTreeInfo(
                    level,
                    pos,
                    showNumBlocks,
                    showChopsRemaining,
                    chopsComp -> tooltip.addLine(new WrappedComponent(chopsComp)),
                    stack -> line.orElseGet(tooltip::addLine).with(new ItemComponent(stack))
            );
        }
    }
}
