package ht.treechop.client;

import ht.treechop.TreeChop;
import ht.treechop.client.gui.screen.ChopIndicator;
import ht.treechop.client.gui.screen.ClientSettingsScreen;
import ht.treechop.client.model.ChoppedLogBakedModel;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.ClientRequestSettingsPacket;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import net.minecraft.client.Minecraft;
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
public class Client {

    private static final ClientChopSettings chopSettings = new ClientChopSettings();
    private static final Permissions serverPermissions = new Permissions();

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(EventHandler.class);

        if (ConfigHandler.CLIENT.useProceduralChoppedModels.get()) {
            IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
            modBus.addListener(ChoppedLogBakedModel::overrideBlockStateModels);
        }
    }

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("chopping_indicator", ChopIndicator::render);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        KeyBindings.registerKeyMappings(event::register);
    }

    static class EventHandler {
        @SubscribeEvent
        public static void onConnect(ClientPlayerNetworkEvent.LoggingIn event) {
            TreeChop.LOGGER.info("Sending chop settings sync request");
            chopSettings.copyFrom(ConfigHandler.CLIENT.getChopSettings());
            PacketHandler.sendToServer(new ClientRequestSettingsPacket(chopSettings));
        }

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (!event.isCanceled() && event.getKey() != GLFW.GLFW_KEY_UNKNOWN) {
                KeyBindings.buttonPressed(event.getKey(), event.getAction());
            }
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

    public static void updatePermissions(Permissions permissions) {
        serverPermissions.copy(permissions);
    }

    public static Permissions getServerPermissions() {
        return serverPermissions;
    }
}
