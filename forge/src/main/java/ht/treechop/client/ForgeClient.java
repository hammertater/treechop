package ht.treechop.client;

import ht.treechop.client.gui.screen.ChopIndicator;
import ht.treechop.client.model.ForgeChoppedLogBakedModel;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.network.ForgePacketHandler;
import ht.treechop.common.registry.ForgeModBlocks;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.OverlayRegistry;
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
        KeyBindings.registerKeyMappings(ClientRegistry::registerKeyBinding);
        ItemBlockRenderTypes.setRenderLayer(ForgeModBlocks.CHOPPED_LOG.get(), RenderType.solid());

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ForgeChoppedLogBakedModel::overrideBlockStateModels);

        OverlayRegistry.registerOverlayTop("treechop:chopping_indicator",
                (gui, poseStack, partialTick, width, height) -> ChopIndicator.render(poseStack, width, height));
    }

    @Override
    void sendToServer(CustomPacket packet) {
        ForgePacketHandler.HANDLER.sendToServer(packet);
    }

    static class EventHandler {
        @SubscribeEvent
        public static void onConnect(ClientPlayerNetworkEvent.LoggedInEvent event) {
            syncOnJoin();
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.KeyInputEvent event) {
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
