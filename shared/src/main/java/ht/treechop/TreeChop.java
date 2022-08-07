package ht.treechop;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class TreeChop {
    public static final String MOD_ID = "treechop";
    public static final String MOD_NAME = "HT's TreeChop";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("ConstantConditions")
    public static void showText(String text) {
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(String.format("%s[%s] %s%s", ChatFormatting.GRAY, TreeChop.MOD_NAME, ChatFormatting.WHITE, text)));
    }
}
