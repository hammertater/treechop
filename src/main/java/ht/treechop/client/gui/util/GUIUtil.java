package ht.treechop.client.gui.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class GUIUtil {
    public static final int TEXT_LINE_HEIGHT = 8;
    public static final int BUTTON_HEIGHT = 20;

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

    public static void renderTooltip(PoseStack poseStack) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null && tooltipText != null) {
            screen.renderTooltip(poseStack, tooltipText, tooltipX, tooltipY);
        }

        tooltipText = null;
    }
}
