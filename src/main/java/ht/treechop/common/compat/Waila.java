package ht.treechop.common.compat;

import ht.treechop.TreeChopMod;
import ht.treechop.common.block.ChoppedLogBlock;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import net.minecraft.world.level.block.Block;

@WailaPlugin(id = TreeChopMod.MOD_ID + ":waila_plugin")
public class Waila implements IWailaPlugin {
    @Override
    public void register(IRegistrar registrar) {
        registrar.addConfig(WailaTreeInfo.SHOW_TREE_BLOCKS, false);
        registrar.addConfig(WailaTreeInfo.SHOW_NUM_CHOPS_REMAINING, true);
        registrar.addComponent(WailaTreeInfo.INSTANCE, TooltipPosition.BODY, Block.class);
        registrar.addOverride(WailaTreeInfo.INSTANCE, ChoppedLogBlock.class);
    }
}
