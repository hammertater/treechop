package ht.treechop;

import ht.treechop.common.platform.Platform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TreeChop {
    public static final String MOD_ID = "treechop";
    public static final String MOD_NAME = "HT's TreeChop";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Platform platform;
    public static TreeChopInternalAPI api;

    @SuppressWarnings("ConstantConditions")
    public static void showText(String text) {
        Minecraft.getInstance().player.displayClientMessage(Component.literal(String.format("%s[%s] %s%s", ChatFormatting.GRAY, TreeChop.MOD_NAME, ChatFormatting.WHITE, text)), false);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(TreeChop.MOD_ID, path);
    }
}
