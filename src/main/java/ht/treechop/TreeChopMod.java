package ht.treechop;

import com.mojang.logging.LogUtils;
import ht.treechop.common.Common;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.init.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TreeChopMod.MOD_ID)
public class TreeChopMod {
    public static final String MOD_ID = "treechop";
    public static final String MOD_NAME = "HT's TreeChop";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TreeChopMod() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        modBus.addListener((FMLCommonSetupEvent e) -> ConfigHandler.onReload());
        modBus.addListener((ModConfigEvent.Reloading e) -> ConfigHandler.onReload());

        modBus.addListener(Common::onCommonSetup);

        ModBlocks.BLOCKS.register(modBus);
        ModBlocks.ENTITIES.register(modBus);
    }

    @SuppressWarnings("ConstantConditions")
    public static void showText(String text) {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.format("%s[%s] %s%s", ChatFormatting.GRAY, TreeChopMod.MOD_NAME, ChatFormatting.WHITE, text)));
    }

}
