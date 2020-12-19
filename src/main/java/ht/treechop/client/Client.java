package ht.treechop.client;

import ht.treechop.TreeChopMod;
import ht.treechop.common.Common;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.PacketEnableChopping;
import ht.treechop.common.network.PacketEnableFelling;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.network.PacketSetSneakBehavior;
import ht.treechop.common.network.PacketSyncChopSettingsToServer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Client extends Common {

    private static final ChopSettings chopSettings = new ChopSettings();
    private static boolean pendingSync = false;

    /**
     * This is too early to send packets; as a hacky workaround, let's delay syncing until the next EntityJoinWorldEvent
     */
    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        pendingSync = true;
    }

    @SubscribeEvent
    public void onConnectDelayed(EntityJoinWorldEvent event) {
        if (pendingSync && event.getEntity() == Minecraft.getMinecraft().player) {
            updateChopSettings(ConfigHandler.getChopSettings());
            pendingSync = false;
        }
    }

    public static void updateChopSettings(ChopSettings chopSettingsIn) {
        TreeChopMod.LOGGER.info("Sending chop settings sync request");
        chopSettings.copyFrom(chopSettingsIn);
        PacketHandler.sendToServer(new PacketSyncChopSettingsToServer(chopSettingsIn));
    }

    @Override
    public void preInit() {
        super.preInit();
        KeyBindings.init();
    }

    @Override
    public boolean isClient() {
        return true;
    }

    public static void toggleChopping() {
        chopSettings.toggleChopping();
        PacketHandler.sendToServer(new PacketEnableChopping(chopSettings.getChoppingEnabled()));
    }

    public static void toggleFelling() {
        chopSettings.toggleFelling();
        PacketHandler.sendToServer(new PacketEnableFelling(chopSettings.getFellingEnabled()));
    }

    public static void cycleSneakBehavior() {
        chopSettings.cycleSneakBehavior();
        PacketHandler.sendToServer(new PacketSetSneakBehavior(chopSettings.getSneakBehavior()));
    }

    public static ChopSettings getChopSettings() {
        return chopSettings;
    }

}
