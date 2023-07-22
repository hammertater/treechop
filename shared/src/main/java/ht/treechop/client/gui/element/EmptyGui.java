package ht.treechop.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;

public class EmptyGui extends NestedGui {

    private int width;
    private int height;

    public EmptyGui(int width, int height) {
        super(0, 0, width, height, Component.empty());
        this.width = width;
        this.height = height;
        active = false;
    }

    public int getMinimumWidth() {
        return width;
    }

    public int getMinimumHeight() {
        return height;
    }

    public int getLeftColumnWidth() {
        return 0;
    }

    public int getRightColumnWidth() {
        return 0;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public void renderWidget(GuiGraphics poseStack, int mouseX, int mouseY, float partialTicks) {
    }

    public void setColumnWidths(int biggestLeftColumnWidth, int biggestRightColumnWidth) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }
}
