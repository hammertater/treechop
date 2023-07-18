package ht.treechop;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.api.config.v2.ModConfigEvents;
import ht.treechop.api.ITreeChopAPIProvider;
import ht.treechop.common.FabricCommon;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.platform.FabricPlatform;
import ht.treechop.common.registry.FabricModBlocks;
import ht.treechop.compat.TreeChopFabricAPITest;
import ht.treechop.server.FabricServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.fml.config.ModConfig;

public class TreeChopFabric extends TreeChop implements ModInitializer {

    @Override
    public void onInitialize() {
        platform = new FabricPlatform();
        api = new TreeChopFabricAPI(TreeChop.MOD_ID);

        PlayerBlockBreakEvents.BEFORE.register(FabricCommon::onBreakEvent);

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> ConfigHandler.updateTags());

        ModConfigEvents.reloading(TreeChop.MOD_ID).register(TreeChopFabric::onReload);
        ForgeConfigRegistry.INSTANCE.register(TreeChop.MOD_ID, ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ForgeConfigRegistry.INSTANCE.register(TreeChop.MOD_ID, ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        Registry.register(BuiltInRegistries.BLOCK, resource("chopped_log"), FabricModBlocks.CHOPPED_LOG);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, resource("chopped_log_entity"), FabricModBlocks.CHOPPED_LOG_ENTITY);

        FabricServer.initialize();
        FabricLoader.getInstance().getObjectShare().put("treechop:api_provider", (ITreeChopAPIProvider) TreeChopFabricAPI::new);

        TreeChopFabricAPITest.init();
    }

    private static void onReload(ModConfig config) {
        if (config.getModId().equals(TreeChop.MOD_ID)) {
            ConfigHandler.onReload();
        }
    }
}
