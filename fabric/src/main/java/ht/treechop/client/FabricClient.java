package ht.treechop.client;

import ht.treechop.TreeChop;
import ht.treechop.client.model.*;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.ServerConfirmSettingsPacket;
import ht.treechop.common.network.ServerPermissionsPacket;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import ht.treechop.common.registry.FabricModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FabricClient extends Client implements ClientModInitializer {
    static {
        Client.instance = new FabricClient();
    }

    @Override
    public void onInitializeClient() {
        Supplier<ChoppedLogBakedModel> modelGetter;
        if (FabricLoader.getInstance().isModLoaded("sodium") && !FabricLoader.getInstance().isModLoaded("indium")) {
            TreeChop.LOGGER.info("Sodium detected! Using alternative block renderer.");
            ConfigHandler.removeBarkOnInteriorLogs.override(false);
            modelGetter = HiddenChoppedLogBakedModel::new;
            BlockEntityRenderers.register(FabricModBlocks.CHOPPED_LOG_ENTITY, FabricChoppedLogEntityRenderer::new);
        } else {
            modelGetter = FabricChoppedLogBakedModel::new;
            BlockRenderLayerMap.INSTANCE.putBlock(FabricModBlocks.CHOPPED_LOG, ChoppedLogBakedModel.RENDER_TYPE);
        }

        ModelLoadingPlugin.register(new ChoppedLogModelLoadingPlugin(modelGetter));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> syncOnJoin());

        registerPackets();
        registerKeybindings();
    }

    private void registerKeybindings() {
        KeyBindings.registerKeyMappings(KeyBindingHelper::registerKeyBinding);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            for (KeyBindings.ActionableKeyBinding keyBinding : KeyBindings.allKeyBindings) {
                if (keyBinding.consumeClick()) {
                    keyBinding.onPress();
                    return;
                }
            }
        });
    }

    private void registerPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ServerConfirmSettingsPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(ServerPermissionsPacket.TYPE, (payload, context) -> payload.handle());
        ClientPlayNetworking.registerGlobalReceiver(ServerUpdateChopsPacket.TYPE, (payload, context) -> payload.handle());
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }
}
