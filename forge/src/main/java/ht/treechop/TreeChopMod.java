package ht.treechop;

import ht.treechop.common.Common;
import ht.treechop.common.config.ForgeConfigHandler;
import ht.treechop.common.init.ModBlocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TreeChop.MOD_ID)
public class TreeChopMod extends TreeChop {

    public TreeChopMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ForgeConfigHandler.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ForgeConfigHandler.CLIENT_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener((FMLCommonSetupEvent e) -> ForgeConfigHandler.onReload());
        modBus.addListener((ModConfigEvent.Reloading e) -> ForgeConfigHandler.onReload());

        modBus.addListener(Common::onCommonSetup);

        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ENTITIES.register(modBus);
    }

}
