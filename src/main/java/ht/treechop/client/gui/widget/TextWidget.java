package ht.treechop.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

public class TextWidget extends Widget {

    private FontRenderer font;

    public TextWidget(int x, int y, FontRenderer font, ITextComponent text) {
        super(x, y, font.getStringWidth(text.getString()), GUIUtil.TEXT_LINE_HEIGHT, text);
        this.font = font;
    }

    @SuppressWarnings({"SuspiciousNameCombination", "NullableProblems"})
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        drawString(matrixStack, font, getMessage(), x, y, 0xFFFFFF);
    }

}
