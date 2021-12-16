package ht.treechop.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class GUIUtil {
    public static final int TEXT_LINE_HEIGHT = 8;
    public static final int BUTTON_HEIGHT = 20;

    public static int getTextWidth(Component name) {
        return getTextWidth(name.getString());
    }

    public static int getTextWidth(String name) {
        Font font = Minecraft.getInstance().font;
        return font.width(name);
    }

    public static int getMinimumButtonWidth(Component name) {
        return GUIUtil.getTextWidth(name) + 9;
    }
}
