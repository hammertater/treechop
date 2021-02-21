package ht.treechop;

import ht.treechop.client.Client;
import ht.treechop.client.gui.screen.ConfigScreen;
import ht.treechop.common.Common;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("treechop")
public class TreeChopMod {
    public static final String MOD_ID = "treechop";
    public static final String MOD_NAME = "HT's TreeChop";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public TreeChopMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener((ModConfig.Loading e) -> ConfigHandler.onReload());
        modBus.addListener((ModConfig.Reloading e) -> ConfigHandler.onReload());

        modBus.addListener(Common::onCommonSetup);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Client::init);

        ModBlocks.BLOCKS.register(modBus);

        ModLoadingContext.get().registerExtensionPoint(
                ExtensionPoint.CONFIGGUIFACTORY,
                () -> ConfigScreen::new
        );
    }

    public static ITextComponent makeText(String string) {
        return new StringTextComponent(TextFormatting.GRAY + "[TreeChop] " + TextFormatting.WHITE + string);
    }
}
