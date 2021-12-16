package ht.treechop.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.gui.util.IPositionalGui;
import ht.treechop.client.gui.util.ScreenBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public abstract class NestedGui extends AbstractWidget implements ContainerEventHandler, IPositionalGui {

    private GuiEventListener listener = null;
    private boolean dragging = false;
    private ScreenBox box;

    public NestedGui(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

    public abstract void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks);

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
    public boolean isDragging() {
        return this.dragging;
    }

    @Override
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        this.listener = listener;
    }

    @Nullable
    public GuiEventListener getFocused() {
        return this.listener;
    }

    @Override
    public ScreenBox getBox() {
        return this.box;
    }

    @Override
    public void setBox(ScreenBox box) {
        this.box = box;
    }
}
