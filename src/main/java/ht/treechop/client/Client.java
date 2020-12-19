package ht.treechop.client;

import ht.treechop.TreeChopMod;
import ht.treechop.common.Common;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.network.PacketEnableChopping;
import ht.treechop.common.network.PacketEnableFelling;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.network.PacketSetSneakBehavior;
import ht.treechop.common.network.PacketSyncChopSettings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Client extends Common {

    private static final ChopSettings chopSettings = new ChopSettings();

    @SubscribeEvent
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        TreeChopMod.LOGGER.info("Sending chop settings sync request");
        updateChopSettings(chopSettings);
    }

    public static void updateChopSettings(ChopSettings chopSettingsIn) {
        if (Minecraft.getMinecraft().world != null) {
            chopSettings.copyFrom(chopSettingsIn);
            PacketHandler.sendToServer(new PacketSyncChopSettings(chopSettingsIn));
        }
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
