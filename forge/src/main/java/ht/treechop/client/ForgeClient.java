package ht.treechop.client;

import ht.treechop.client.gui.screen.ChopIndicator;
import ht.treechop.client.model.ForgeChoppedLogBakedModel;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ForgePacketHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClient extends Client {
    static {
        Client.instance = new ForgeClient();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);

        if (ConfigHandler.CLIENT.useProceduralChoppedModels.get()) {
            IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
            modBus.addListener(ForgeChoppedLogBakedModel::overrideBlockStateModels);
        }

    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("chopping_indicator", (gui, poseStack, partialTicks, windowWidth, windowHeight) -> ChopIndicator.render(poseStack, windowWidth, windowHeight));
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyBindings.registerKeyMappings(event::register);
    }

    @Override
    void sendToServer(CustomPacket packet) {
        ForgePacketHandler.HANDLER.sendToServer(packet);
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
