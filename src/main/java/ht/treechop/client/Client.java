package ht.treechop.client;

import ht.treechop.TreeChopMod;
import ht.treechop.client.model.ChoppedLogBakedModel;
import ht.treechop.common.capabilities.ChopSettings;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import ht.treechop.common.network.PacketEnableChopping;
import ht.treechop.common.network.PacketEnableFelling;
import ht.treechop.common.network.PacketHandler;
import ht.treechop.common.network.PacketSetSneakBehavior;
import ht.treechop.common.network.PacketSyncChopSettings;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Client {

    private static final ChopSettings chopSettings = new ChopSettings();

    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(ModBlocks.CHOPPED_LOG.get(), RenderType.getSolid());
    }

    public static void init() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        if (ConfigHandler.CLIENT.useProceduralChoppedModels.get()) {
            modBus.addListener(ChoppedLogBakedModel::overrideBlockStateModels);
        }

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(Client::onClientSetup);
        eventBus.addListener(Client::onConnect);

        KeyBindings.init();
    }

    // TODO: is this working?
    public static void onConnect(ClientPlayerNetworkEvent.LoggedInEvent event) {
        chopSettings.copyFrom(ConfigHandler.CLIENT.getChopSettings());
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
