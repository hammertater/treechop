package ht.treechop;

import ht.treechop.api.TreeChopAPI;
import ht.treechop.common.platform.Platform;
import ht.treechop.compat.HugeFungusHandler;
import ht.treechop.compat.HugeMushroomHandler;
import ht.treechop.compat.ProblematicLeavesTreeHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TreeChop {
    public static final String MOD_ID = "treechop";
    public static final String MOD_NAME = "HT's TreeChop";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static Platform platform;
    public static TreeChopInternalAPI api;

    public static void initUsingAPI(TreeChopAPI api) {
        HugeMushroomHandler.register(api);
        HugeFungusHandler.register(api);
        ProblematicLeavesTreeHandler.register(api);
    }

    @SuppressWarnings("ConstantConditions")
    public static void showText(String text) {
        Minecraft.getInstance().player.displayClientMessage(Component.literal(String.format("%s[%s] %s%s", ChatFormatting.GRAY, TreeChop.MOD_NAME, ChatFormatting.WHITE, text)), false);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(TreeChop.MOD_ID, path);
    }
}
