package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

public class TextWidget extends Widget {

    private final ITextComponent text;
    private FontRenderer font;

    public TextWidget(int x, int y, FontRenderer font, ITextComponent text) {
        super(x, y, font.getStringWidth(text.getUnformattedText()), GUIUtil.TEXT_LINE_HEIGHT);
        this.font = font;
        this.text = text;
    }

    public void render(int mouseX, int mouseY, float partialTicks, boolean rightAligned) {
        render(mouseX, mouseY, partialTicks, rightAligned ? -font.getStringWidth(text.getUnformattedText()) : 0);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        render(mouseX, mouseY, partialTicks, 0);
    }

    public void render(int mouseX, int mouseY, float partialTicks, int xOffset) {
        super.render(mouseX, mouseY, partialTicks);
        drawString(text, getBox().getLeft() + xOffset, getBox().getTop(), 0xFFFFFF);
    }

}
