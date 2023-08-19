package ht.treechop.client.gui.util;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class GUIUtil {
    public static final int TEXT_LINE_HEIGHT = 8;
    public static final int BUTTON_HEIGHT = 20;
    public static int TOOLTIP_WIDTH_BUFFER = 20;

    private static int tooltipX;
    private static int tooltipY;
    private static Component tooltipText;

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

    public static void showTooltip(int x, int y, Component text) {
        tooltipX = x;
        tooltipY = y;
        tooltipText = text;
    }

    public static void renderTooltip(GuiGraphics gui) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null && tooltipText != null) {
            int maxWidth = Math.max(Math.max(tooltipX, screen.width - tooltipX) - TOOLTIP_WIDTH_BUFFER, TOOLTIP_WIDTH_BUFFER);
            Font font = Minecraft.getInstance().font;
            List<FormattedCharSequence> splitText = font.split(tooltipText, maxWidth);
            gui.renderTooltip(font, splitText, tooltipX, tooltipY);
        }

        tooltipText = null;
    }
}
