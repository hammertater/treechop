package ht.treechoptests;

import ht.treechoptests.registry.FabricModBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;

import static ht.treechop.TreeChop.resource;

public class TreeChopFabricTests implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, resource("skinny_log"), FabricModBlocks.SKINNY_LOG);
        Registry.register(Registry.ITEM, resource("skinny_log"), new BlockItem(FabricModBlocks.SKINNY_LOG, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));
    }
}
