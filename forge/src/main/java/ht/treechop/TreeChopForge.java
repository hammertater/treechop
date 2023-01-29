package ht.treechop;

import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.ForgePlatform;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.network.ForgePacketHandler;
import ht.treechop.common.registry.ForgeModBlocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(TreeChop.MOD_ID)
public class TreeChopForge extends TreeChop {

    public TreeChopForge() {
        platform = new ForgePlatform();
        api = new TreeChopForgeAPI(TreeChop.MOD_ID);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        // Listeners for the mod bus
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener((ModConfigEvent.Reloading event) -> ConfigHandler.onReload());
        modBus.addListener(this::processIMC);

        ForgeModBlocks.BLOCKS.register(modBus);
        ForgeModBlocks.ENTITIES.register(modBus);

        // Listeners for the Forge bus
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(event -> ConfigHandler.updateTags());

        ForgePacketHandler.registerPackets();
    }

    private void processIMC(InterModProcessEvent event) {
        event.getIMCStream(message -> message.equalsIgnoreCase("getTreeChopAPI"))
                .forEach(action -> {
                    Supplier<?> supplier = action.messageSupplier();
                    try {
                        @SuppressWarnings("unchecked")
                        Consumer<TreeChopAPI> consumer = (Consumer<TreeChopAPI>) supplier.get();
                        consumer.accept(new TreeChopForgeAPI(action.senderModId()));
                    } catch (ClassCastException | NullPointerException e) {
                        TreeChop.LOGGER.error(String.format("Failed to process getTreeChopAPI request from %s: %s", action.senderModId(), e.getMessage()));
                        e.printStackTrace();
                    }
                });
    }
}
