package ht.treechoptests;

import ht.treechop.api.ITreeChopAPIProvider;
import ht.treechop.api.TreeChopAPI;
import ht.treechop.api.TreeChopEvents;
import ht.treechop.compat.TreeChopFabricAPITest;
import ht.treechoptests.registry.FabricModBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;

public class TreeChopFabricTests implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, resource("test"), FabricModBlocks.TEST_BLOCK);
        Registry.register(Registry.ITEM, resource("test"), new BlockItem(FabricModBlocks.TEST_BLOCK, new FabricItemSettings().group(CreativeModeTab.TAB_MISC)));

        FabricLoader.getInstance().getObjectShare().whenAvailable("treechop:api_provider", (key, value) -> {
            if (value instanceof ITreeChopAPIProvider provider) {
                TreeChopAPI api = provider.get("treechop");
                // Do API stuff
            }
        });
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation("treechoptests", path);
    }
}
