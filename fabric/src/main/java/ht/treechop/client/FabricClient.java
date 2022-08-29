package ht.treechop.client;

import ht.treechop.TreeChop;
import ht.treechop.client.model.ChoppedLogModelProvider;
import ht.treechop.common.block.ChoppedLogBlock;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.MenuType;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class FabricClient extends Client implements ClientModInitializer {
    static {
        Client.instance = new FabricClient();
    }

    @Override
    public void onInitializeClient() {
        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> new ChoppedLogModelProvider());

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
