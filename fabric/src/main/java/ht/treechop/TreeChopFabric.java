package ht.treechop;

import fuzs.forgeconfigapiport.fabric.api.forge.v4.ForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import ht.treechop.api.ITreeChopAPIProvider;
import ht.treechop.common.FabricCommon;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.loot.CountBlockChopsLootItemCondition;
import ht.treechop.common.loot.TreeFelledLootItemCondition;
import ht.treechop.common.network.ClientRequestSettingsPacket;
import ht.treechop.common.network.ServerConfirmSettingsPacket;
import ht.treechop.common.network.ServerPermissionsPacket;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import ht.treechop.common.platform.FabricPlatform;
import ht.treechop.common.registry.FabricModBlocks;
import ht.treechop.compat.TreeChopFabricAPITest;
import ht.treechop.server.FabricServer;
import ht.treechop.server.commands.ServerCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.fml.config.ModConfig;

public class TreeChopFabric extends TreeChop implements ModInitializer {

    @Override
    public void onInitialize() {
        platform = new FabricPlatform();
        api = new TreeChopFabricAPI(TreeChop.MOD_ID);

        PlayerBlockBreakEvents.BEFORE.register(FabricCommon::onBreakEvent);

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> ConfigHandler.updateTags());

        // Config
        NeoForgeModConfigEvents.reloading(TreeChop.MOD_ID).register(TreeChopFabric::onReload);
        ForgeConfigRegistry.INSTANCE.register(TreeChop.MOD_ID, ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ForgeConfigRegistry.INSTANCE.register(TreeChop.MOD_ID, ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        Registry.register(BuiltInRegistries.BLOCK, resource("chopped_log"), FabricModBlocks.CHOPPED_LOG);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, resource("chopped_log_entity"), FabricModBlocks.CHOPPED_LOG_ENTITY);

        // Networking
        PayloadTypeRegistry<RegistryFriendlyByteBuf> r;
        r = PayloadTypeRegistry.playS2C();
        r.register(ServerConfirmSettingsPacket.TYPE, ServerConfirmSettingsPacket.STREAM_CODEC);
        r.register(ServerPermissionsPacket.TYPE, ServerPermissionsPacket.STREAM_CODEC);
        r.register(ServerUpdateChopsPacket.TYPE, ServerUpdateChopsPacket.STREAM_CODEC);

        r = PayloadTypeRegistry.playC2S();
        r.register(ClientRequestSettingsPacket.TYPE, ClientRequestSettingsPacket.STREAM_CODEC);

        // Init server
        FabricServer.initialize();
        FabricLoader.getInstance().getObjectShare().put("treechop:api_provider", (ITreeChopAPIProvider) TreeChopFabricAPI::new);

        // Compat
        TreeChopFabricAPITest.init();

        Registry.register(BuiltInRegistries.SOUND_EVENT, CHOP_WOOD, CHOP_WOOD_EVENT.get());

        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, CountBlockChopsLootItemCondition.ID, CountBlockChopsLootItemCondition.TYPE);
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, TreeFelledLootItemCondition.ID, TreeFelledLootItemCondition.TYPE);

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            ServerCommands.register(dispatcher);
        }));
    }

    private static void onReload(ModConfig config) {
        if (config.getModId().equals(TreeChop.MOD_ID)) {
            ConfigHandler.onReload();
        }
    }
}
