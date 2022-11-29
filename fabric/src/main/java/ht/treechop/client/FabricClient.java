package ht.treechop.client;

import ht.treechop.TreeChop;
import ht.treechop.client.model.*;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ServerConfirmSettingsPacket;
import ht.treechop.common.network.ServerPermissionsPacket;
import ht.treechop.common.network.ServerUpdateChopsPacket;
import ht.treechop.common.registry.FabricModBlocks;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;

@Environment(EnvType.CLIENT)
public class FabricClient extends Client implements ClientModInitializer {
    static {
        Client.instance = new FabricClient();
    }

    public static ChoppedLogBakedModel choppedLogModel = new FabricChoppedLogBakedModel();

    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("sodium")) {
            TreeChop.LOGGER.info("Sodium detected! Using alternative block renderer.");
            ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> new ChoppedLogModelProvider(new HiddenChoppedLogBakedModel()));
            BlockEntityRendererRegistry.register(FabricModBlocks.CHOPPED_LOG_ENTITY, FabricChoppedLogEntityRenderer::new);
        } else {
            ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> new ChoppedLogModelProvider(choppedLogModel));
        }

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
        // Note: buffers get freed before deferred code can run, so we must process them immediately
        ClientPlayNetworking.registerGlobalReceiver(ServerConfirmSettingsPacket.ID, (client, handler, buffer, sender) -> {
            ServerConfirmSettingsPacket packet = ServerConfirmSettingsPacket.decode(buffer);
            client.execute(() -> ServerConfirmSettingsPacket.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerPermissionsPacket.ID, (client, handler, buffer, sender) -> {
            ServerPermissionsPacket packet = ServerPermissionsPacket.decode(buffer);
            client.execute(() -> ServerPermissionsPacket.handle(packet));
        });

        ClientPlayNetworking.registerGlobalReceiver(ServerUpdateChopsPacket.ID, (client, handler, buffer, sender) -> {
            ServerUpdateChopsPacket packet = ServerUpdateChopsPacket.decode(buffer);
            client.execute(() -> ServerUpdateChopsPacket.handle(packet));
        });
    }

    @Override
    void sendToServer(CustomPacket packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ClientPlayNetworking.send(packet.getId(), packet.encode(buffer));
    }
}
