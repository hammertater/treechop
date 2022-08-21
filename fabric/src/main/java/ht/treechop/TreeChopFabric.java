package ht.treechop;

import ht.treechop.common.FabricCommon;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.platform.FabricPlatform;
import ht.treechop.common.registry.FabricModBlocks;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.api.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.config.ModConfig;

public class TreeChopFabric extends TreeChop implements ModInitializer {
    @Override
    public void onInitialize() {
        platform = new FabricPlatform();

        ModLoadingContext.registerConfig(TreeChop.MOD_ID, ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ModLoadingContext.registerConfig(TreeChop.MOD_ID, ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
        PlayerBlockBreakEvents.BEFORE.register(FabricCommon::onBreakEvent);

        ModConfigEvent.RELOADING.register((ModConfig config) -> {
            if (config.getModId().equals(TreeChop.MOD_ID)) {
                ConfigHandler.onReload();
            }
        });
    }
}
