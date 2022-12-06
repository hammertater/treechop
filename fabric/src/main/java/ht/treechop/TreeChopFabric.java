package ht.treechop;

import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.FabricCommon;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.platform.FabricPlatform;
import ht.treechop.common.registry.FabricModBlocks;
import ht.treechop.server.FabricServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.api.fml.event.config.ModConfigEvents;
import net.minecraftforge.fml.config.ModConfig;

public class TreeChopFabric extends TreeChop implements ModInitializer {

    @Override
    public void onInitialize() {
        platform = new FabricPlatform();
        api = new TreeChopFabricAPI(TreeChop.MOD_ID);

        ModLoadingContext.registerConfig(TreeChop.MOD_ID, ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ModLoadingContext.registerConfig(TreeChop.MOD_ID, ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
        PlayerBlockBreakEvents.BEFORE.register(FabricCommon::onBreakEvent);

        ModConfigEvents.reloading(TreeChop.MOD_ID).register((ModConfig config) -> {
            if (config.getModId().equals(TreeChop.MOD_ID)) {
                ConfigHandler.onReload();
            }
        });

        Registry.register(Registry.BLOCK, resource("chopped_log"), FabricModBlocks.CHOPPED_LOG);
        Registry.register(Registry.BLOCK_ENTITY_TYPE, resource("chopped_log_entity"), FabricModBlocks.CHOPPED_LOG_ENTITY);

        FabricServer.initialize();
        FabricLoader.getInstance().getObjectShare().put("treechop:api", api);

        // This is totally unnecessary - just testing to make sure it works
        TreeChopAPI apiTest = (TreeChopAPI) FabricLoader.getInstance().getObjectShare().get("treechop:api");
        initUsingAPI(apiTest);
    }
}
