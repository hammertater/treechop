package ht.treechop;

import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.ForgeCommon;
import ht.treechop.common.ForgePlatform;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.registry.ForgeModBlocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
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

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(ForgeCommon::onCommonSetup);
        modBus.addListener((ModConfigEvent.Reloading e) -> ConfigHandler.onReload());
        modBus.addListener(this::processIMC);
        modBus.addListener(this::enqueueIMC);

        ForgeModBlocks.BLOCKS.register(modBus);
        ForgeModBlocks.ENTITIES.register(modBus);
    }

    private void enqueueIMC(InterModEnqueueEvent event) {
        // This is totally unnecessary - just testing to make sure it works
        if (ConfigHandler.COMMON.compatForMushroomStems.get()) {
            InterModComms.sendTo("treechop", "getTreeChopAPI", () -> (Consumer<TreeChopAPI>) this::initUsingAPI);
        }
    }

    private void processIMC(InterModProcessEvent event) {
        event.getIMCStream(message -> message.equalsIgnoreCase("getTreeChopAPI"))
                .forEach(action -> {
                    Supplier<?> supplier = action.messageSupplier();
                    try {
                        Consumer<TreeChopAPI> consumer = (Consumer<TreeChopAPI>) supplier.get();
                        consumer.accept(new TreeChopForgeAPI(action.senderModId()));
                    } catch (ClassCastException | NullPointerException e) {
                        TreeChop.LOGGER.error(String.format("Failed to process getTreeChopAPI request from %s: %s", action.senderModId(), e.getMessage()));
                        e.printStackTrace();
                    }
                });
    }
}
