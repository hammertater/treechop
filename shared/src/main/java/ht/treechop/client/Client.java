package ht.treechop.client;

import ht.treechop.TreeChop;
import ht.treechop.client.gui.screen.ClientSettingsScreen;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.ClientRequestSettingsPacket;
import ht.treechop.common.network.CustomPacket;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import net.minecraft.client.Minecraft;

public abstract class Client {
    protected static final ClientChopSettings chopSettings = new ClientChopSettings() {
        @Override
        public ChopSettings set(SettingsField field, Object value) {
            treeUnderCursor.invalidate();
            return super.set(field, value);
        }
    };
    protected static final Permissions serverPermissions = new Permissions();
    protected static Client instance;

    public static TreeCache treeUnderCursor = new TreeCache();

    public static void requestSetting(SettingsField field, Object value) {
        Client.instance().sendToServer(new ClientRequestSettingsPacket(field, value));
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

    public static Client instance() {
        return instance;
    }

    protected static void syncOnJoin() {
        TreeChop.LOGGER.info("Sending chop settings sync request");
        chopSettings.copyFrom(ConfigHandler.CLIENT.getChopSettings());
        Client.instance().sendToServer(new ClientRequestSettingsPacket(chopSettings));
    }

    abstract void sendToServer(CustomPacket packet);
}
