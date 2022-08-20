package ht.treechop;

import ht.treechop.common.Common;
import ht.treechop.common.registry.FabricModBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class TreeChopFabric extends TreeChop implements ModInitializer {

    @Override
    public void onInitialize() {
        PlayerBlockBreakEvents.BEFORE.register(Common::onBreakEvent);

        Registry.register(Registry.BLOCK, new ResourceLocation(MOD_ID, "chopped_log"), FabricModBlocks.CHOPPED_LOG);
    }
}
