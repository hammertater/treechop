package ht.treechop.client;

import ht.treechop.TreeChopMod;
import ht.treechop.client.gui.screen.ChopIndicator;
import ht.treechop.client.gui.screen.ClientSettingsScreen;
import ht.treechop.client.model.ChoppedLogBakedModel;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.network.ClientRequestSettingsPacket;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = TreeChopMod.MOD_ID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class Client {

    private static final ClientChopSettings chopSettings = new ClientChopSettings();
    private static final ChopIndicator chopIndicator = new ChopIndicator();
    private static final Permissions serverPermissions = new Permissions();

    public static void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (ConfigHandler.CLIENT.useProceduralChoppedModels.get()) {
            modBus.addListener(ChoppedLogBakedModel::overrideBlockStateModels);
        }

        if (ConfigHandler.CLIENT.showChoppingIndicators.get()) {
            MinecraftForge.EVENT_BUS.addListener(Client::renderOverlay);
        }

        KeyBindings.init();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ModBlocks.CHOPPED_LOG.get(), RenderType.solid());
    }

    @SubscribeEvent
    public static void onConnect(ClientPlayerNetworkEvent.LoggedInEvent event) {
        TreeChopMod.LOGGER.info("Sending chop settings sync request");
        chopSettings.copyFrom(ConfigHandler.CLIENT.getChopSettings());
        PacketHandler.sendToServer(new ClientRequestSettingsPacket(chopSettings));
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        if (
                !event.isCanceled()
                && event.getKey() != GLFW.GLFW_KEY_UNKNOWN
        ) {
            KeyBindings.buttonPressed(event.getKey(), event.getAction());
        }
    }

    public static void requestSetting(SettingsField field, Object value) {
        PacketHandler.sendToServer(new ClientRequestSettingsPacket(field, value));
    }

    public static void toggleChopping() {
        boolean newValue = !chopSettings.get(SettingsField.CHOPPING, Boolean.class);
        chopSettings.set(SettingsField.CHOPPING, newValue);
    }

    public static void toggleFelling() {
        boolean newValue = !chopSettings.get(SettingsField.FELLING, Boolean.class);
        chopSettings.set(SettingsField.FELLING, newValue);
    }

    public static void cycleSneakBehavior() {
        SneakBehavior newValue = ConfigHandler.CLIENT.showFellingOptions.get()
                ? chopSettings.getSneakBehavior().cycle()
                : (chopSettings.getSneakBehavior() == SneakBehavior.NONE ? SneakBehavior.INVERT_CHOPPING : SneakBehavior.NONE);
        chopSettings.set(SettingsField.SNEAK_BEHAVIOR, newValue);
    }

    public static ClientChopSettings getChopSettings() {
        return chopSettings;
    }

    public static void setChoppingIndicatorVisibility(boolean showChoppingIndicator) {
        ConfigHandler.CLIENT.showChoppingIndicators.set(showChoppingIndicator);
    }

    public static boolean isChoppingIndicatorEnabled() {
        return ConfigHandler.CLIENT.showChoppingIndicators.get();
    }

    public static void toggleSettingsOverlay() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof ClientSettingsScreen) {
            minecraft.screen.onClose();
        } else {
            minecraft.setScreen(new ClientSettingsScreen());
        }
    }

    public static void renderOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            chopIndicator.render(
                    event.getWindow(),
                    event.getMatrixStack(),
                    event.getPartialTicks()
            );
        }
    }

    public static void updatePermissions(Permissions permissions) {
        serverPermissions.copy(permissions);
    }

    public static Permissions getServerPermissions() {
        return serverPermissions;
    }
}
