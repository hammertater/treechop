package ht.treechop.client.gui.element;

import ht.treechop.client.gui.IGuiEventListener;
import ht.treechop.client.gui.util.IPositionalGui;
import ht.treechop.client.gui.util.ScreenBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;

public abstract class NestedGui implements IGuiEventListener, IPositionalGui {

    static private final WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();
    private ScreenBox box = new ScreenBox(0, 0, 0, 0);

    public abstract int getMinimumWidth();

    public abstract int getMinimumHeight();

    public int getLeftColumnWidth() {
        return getMinimumWidth() / 2;
    }

    public int getRightColumnWidth() {
        return getMinimumWidth() / 2;
    }

    public void setColumnWidths(int leftColumnWidth, int rightColumnWidth) {
    }

    @Override
    public ScreenBox getBox() {
        return this.box;
    }

    @Override
    public void setBox(ScreenBox box) {
        this.box = box;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
    }

    @Override
    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= getBox().getLeft() && mouseY >= getBox().getTop() && mouseX < getBox().getLeft() + getBox().getWidth() && mouseY < getBox().getTop() + getBox().getHeight();
    }

    public void blit(int x, int y, int u, int v, int width, int height) {
        blit(x, y, u, v, width, height, 256, 256);
    }

    public void blit(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        Gui.drawModalRectWithCustomSizedTexture(
                x,
                y,
                u,
                v,
                width,
                height,
                textureWidth,
                textureHeight
        );
    }

    public static void drawString(ITextComponent text, int x, int y) {
        final int WHITE = 16777215;
        drawString(text, x, y, WHITE);
    }

    public static void drawString(ITextComponent text, int x, int y, int color) {
        Minecraft.getMinecraft().fontRenderer.drawString(text.getUnformattedText(), x, y, color);
    }

    public static void fill(int minX, int minY, int maxX, int maxY, int color) {
        if (minX < maxX) {
            int i = minX;
            minX = maxX;
            maxX = i;
        }

        if (minY < maxY) {
            int j = minY;
            minY = maxY;
            maxY = j;
        }

        float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos((float)minX, (float)maxY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos((float)maxX, (float)maxY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos((float)maxX, (float)minY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.pos((float)minX, (float)minY, 0.0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.finishDrawing();
        vboUploader.draw(bufferbuilder);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

}
