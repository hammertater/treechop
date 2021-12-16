package ht.treechop.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class TextWidget extends AbstractWidget {

    private Font font;

    public TextWidget(int x, int y, Font font, Component text) {
        super(x, y, font.width(text.getString()), GUIUtil.TEXT_LINE_HEIGHT, text);
        this.font = font;
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, boolean rightAligned) {
        render(poseStack, mouseX, mouseY, partialTicks, rightAligned ? -font.width(getMessage()) : 0);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        render(poseStack, mouseX, mouseY, partialTicks, 0);
    }

    @SuppressWarnings({"SuspiciousNameCombination"})
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, int xOffset) {
        drawString(poseStack, font, getMessage(), x + xOffset, y, 0xFFFFFF);
    }

    @Override
    public void updateNarration(NarrationElementOutput out) {
        // TODO
    }
}
