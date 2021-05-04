package ht.treechop.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.util.IPositionalGui;
import ht.treechop.client.gui.util.ScreenBox;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;

import javax.annotation.Nullable;

public abstract class NestedGui implements INestedGuiEventHandler, IPositionalGui {

    private IGuiEventListener listener = null;
    private boolean dragging = false;
    private ScreenBox box;

    public abstract void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

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
    public void setListener(@Nullable IGuiEventListener listener) {
        this.listener = listener;
    }

    @Nullable
    public IGuiEventListener getListener() {
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
