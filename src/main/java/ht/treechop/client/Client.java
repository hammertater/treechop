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
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Client {

    private static final ChopSettings chopSettings = new ChopSettings();

    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderTypeLookup.setRenderLayer(ModBlocks.CHOPPED_LOG.get(), RenderType.getSolid());
    }

    public static void init() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(Client::onModelBakeEvent);

        IEventBus bus = MinecraftForge.EVENT_BUS;
        bus.addListener(Client::onClientSetup);
        bus.addListener(Client::onConnect);

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

    @SubscribeEvent
    public static void onModelBakeEvent(ModelBakeEvent event) {
        for (BlockState blockState : ModBlocks.CHOPPED_LOG.get().getStateContainer().getValidStates()) {
            ModelResourceLocation variantMRL = BlockModelShapes.getModelLocation(blockState);
            IBakedModel existingModel = event.getModelRegistry().get(variantMRL);
            if (existingModel == null) {
                TreeChopMod.LOGGER.warn("Did not find the expected vanilla baked model(s) for treeworld:chopped_log in registry");
            } else if (existingModel instanceof ChoppedLogBakedModel) {
                TreeChopMod.LOGGER.warn("Tried to replace ChoppedLogBakedModel twice");
            } else {
                ChoppedLogBakedModel customModel = new ChoppedLogBakedModel(existingModel);
                event.getModelRegistry().put(variantMRL, customModel);
            }
        }
    }

}
