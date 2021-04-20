package ht.treechop.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GUIUtil {
    public static final int TEXT_LINE_HEIGHT = 8;
    public static final int BUTTON_HEIGHT = 20;

    public static int getTextWidth(ITextComponent name) {
        return getTextWidth(name.getString());
    }

    public static int getTextWidth(String name) {
        FontRenderer font = Minecraft.getInstance().fontRenderer;
        return font.getStringWidth(name);
    }

    public static int getMinimumButtonWidth(ITextComponent name) {
        return GUIUtil.getTextWidth(name) + 9;
    }
}
