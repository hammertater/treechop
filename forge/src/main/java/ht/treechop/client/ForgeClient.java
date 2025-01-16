package ht.treechop.client;

import ht.treechop.client.gui.screen.ChopIndicator;
import ht.treechop.common.network.ForgePacketHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClient extends Client {
    static {
        Client.instance = new ForgeClient();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);
    }

    @SubscribeEvent
    public static void onRegisterOverlays(CustomizeGuiOverlayEvent event) {
        ChopIndicator.render(event.getGuiGraphics(), event.getWindow().getWidth(), event.getWindow().getHeight());
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyBindings.registerKeyMappings(event::register);
    }

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        ForgePacketHandler.HANDLER.send(payload, PacketDistributor.SERVER.noArg());
    }

    static class EventHandler {
        @SubscribeEvent
        public static void onConnect(ClientPlayerNetworkEvent.LoggingIn event) {
            syncOnJoin();
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (!event.isCanceled() && event.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                for (KeyBindings.ActionableKeyBinding keyBinding : KeyBindings.allKeyBindings) {
                    if (event.getKey() == keyBinding.getKey().getValue() && event.getAction() == GLFW.GLFW_PRESS) {
                        keyBinding.onPress();
                        return;
                    }
                }
            }
        }
    }
}
