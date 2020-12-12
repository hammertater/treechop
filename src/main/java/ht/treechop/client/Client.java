package ht.treechop.client;

import ht.treechop.TreeChopMod;
import ht.treechop.capabilities.ChopSettings;
import ht.treechop.config.ConfigHandler;
import ht.treechop.network.PacketEnableChopping;
import ht.treechop.network.PacketEnableFelling;
import ht.treechop.network.PacketHandler;
import ht.treechop.network.PacketSetSneakBehavior;
import ht.treechop.network.PacketSyncChopSettings;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class Client {

    private static final ChopSettings chopSettings = new ChopSettings();

    public static void onClientSetup(FMLClientSetupEvent event) {
        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(Client::onConnect);
        eventBus.addListener(KeyBindings::buttonPressed);
        KeyBindings.clientSetup(event);
    }

    public static void onConnect(ClientPlayerNetworkEvent.LoggedInEvent event) {
        chopSettings.setChoppingEnabled(ConfigHandler.CLIENT.choppingEnabled.get());
        chopSettings.setFellingEnabled(ConfigHandler.CLIENT.fellingEnabled.get());
        chopSettings.setSneakBehavior(ConfigHandler.CLIENT.sneakBehavior.get());

        TreeChopMod.LOGGER.info("Sending chop settings sync request");
        PacketHandler.sendToServer(new PacketSyncChopSettings(chopSettings));
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
