package ht.treechop.compat;

import com.sun.source.tree.Tree;
import ht.treechop.TreeChop;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.config.ConfigHandler;
import mcp.mobius.waila.api.*;
import mcp.mobius.waila.api.component.ItemComponent;
import mcp.mobius.waila.api.component.WrappedComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class Wthit implements IWailaCommonPlugin, IWailaClientPlugin, IBlockComponentProvider {
    public static final ResourceLocation SHOW_TREE_BLOCKS = TreeChop.resource("show_tree_block_counts");
    public static final ResourceLocation SHOW_NUM_CHOPS_REMAINING = TreeChop.resource("show_num_chops_remaining");
    private static final Wthit INSTANCE = new Wthit();

    @Override
    public void register(ICommonRegistrar registrar) {
        registrar.featureConfig(SHOW_TREE_BLOCKS, true);
        registrar.featureConfig(SHOW_NUM_CHOPS_REMAINING, true);
    }

    @Override
    public void register(IClientRegistrar registrar) {
        registrar.body(INSTANCE, Block.class);
        registrar.head(INSTANCE, Block.class);
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
