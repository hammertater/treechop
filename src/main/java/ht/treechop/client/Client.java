package ht.treechop.client;

import ht.treechop.common.Common;
import ht.treechop.common.capabilities.ChopSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Client extends Common {

    private static final ChopSettings chopSettings = new ChopSettings();

//    public static void onClientSetup(FMLClientSetupEvent event) {
//        IEventBus eventBus = MinecraftForge.EVENT_BUS;
//        eventBus.addListener(Client::onConnect);
//        eventBus.addListener(KeyBindings::buttonPressed);
//        KeyBindings.clientSetup(event);
//    }
//
//    public static void onConnect(ClientPlayerNetworkEvent.LoggedInEvent event) {
//        chopSettings.setChoppingEnabled(ConfigHandler.CLIENT.choppingEnabled.get());
//        chopSettings.setFellingEnabled(ConfigHandler.CLIENT.fellingEnabled.get());
//        chopSettings.setSneakBehavior(ConfigHandler.CLIENT.sneakBehavior.get());
//
//        TreeChopMod.LOGGER.info("Sending chop settings sync request");
//        PacketHandler.sendToServer(new PacketSyncChopSettings(chopSettings));
//    }
//
//    public static void toggleChopping() {
//        chopSettings.toggleChopping();
//        PacketHandler.sendToServer(new PacketEnableChopping(chopSettings.getChoppingEnabled()));
//    }
//
//    public static void toggleFelling() {
//        chopSettings.toggleFelling();
//        PacketHandler.sendToServer(new PacketEnableFelling(chopSettings.getFellingEnabled()));
//    }
//
//    public static void cycleSneakBehavior() {
//        chopSettings.cycleSneakBehavior();
//        PacketHandler.sendToServer(new PacketSetSneakBehavior(chopSettings.getSneakBehavior()));
//    }

    public static ChopSettings getChopSettings() {
        return chopSettings;
    }

}
