package ht.treechop;

import ht.treechop.api.TreeChopAPI;
import ht.treechop.client.NeoForgeClientProxy;
import ht.treechop.common.NeoForgePlatform;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.NeoForgeRegistry;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeConfig;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(TreeChop.MOD_ID)
public class TreeChopNeoForge extends TreeChop {

    public TreeChopNeoForge(ModContainer container, IEventBus modBus) {
        platform = new NeoForgePlatform();
        api = new TreeChopNeoForgeAPI(TreeChop.MOD_ID);

        container.registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        NeoForgeRegistry.Blocks.REGISTRY.register(modBus);
        NeoForgeRegistry.BlockEntities.REGISTRY.register(modBus);
        NeoForgeRegistry.LootConditionTypes.REGISTRY.register(modBus);

        modBus.addListener(this::processIMC);
        modBus.addListener((ModConfigEvent.Reloading event) -> ConfigHandler.onReload());
        modBus.addListener((RegisterEvent event) -> event.register(Registries.SOUND_EVENT, helper -> helper.register(CHOP_WOOD, CHOP_WOOD_EVENT.get())));

        NeoForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> ConfigHandler.updateTags());

        if (FMLLoader.getDist() == Dist.CLIENT) {
            NeoForgeClientProxy.init(modBus);
        }

        // TODO: register commands (see fabric)
    }

    private void processIMC(InterModProcessEvent event) {
        event.getIMCStream(message -> message.equalsIgnoreCase("getTreeChopAPI"))
                .forEach(action -> {
                    Supplier<?> supplier = action.messageSupplier();
                    try {
                        @SuppressWarnings("unchecked")
                        Consumer<TreeChopAPI> consumer = (Consumer<TreeChopAPI>) supplier.get();
                        consumer.accept(new TreeChopNeoForgeAPI(action.senderModId()));
                    } catch (ClassCastException | NullPointerException e) {
                        TreeChop.LOGGER.error(String.format("Failed to process getTreeChopAPI request from %s: %s", action.senderModId(), e.getMessage()));
                        e.printStackTrace();
                    }
                });
    }
}
