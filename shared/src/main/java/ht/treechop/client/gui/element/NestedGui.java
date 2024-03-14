package ht.treechop.client.gui.element;

import ht.treechop.client.gui.util.IPositionalGui;
import ht.treechop.client.gui.util.ScreenBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Optional;

public abstract class NestedGui extends AbstractWidget implements ContainerEventHandler, IPositionalGui {

    private GuiEventListener listener = null;
    private boolean dragging = false;
    private ScreenBox box;

    public NestedGui(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }

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
    public void setWidth(int width) {
        ScreenBox box = getBox();
        this.setBox(box.getLeft(), box.getTop(), width, box.getHeight());
    }

    @Override
    public void setBox(ScreenBox box) {
        this.box = box;
        width = box.getWidth();
        height = box.getHeight();
        x = box.getLeft();
        y = box.getTop();
    }

    public Optional<GuiEventListener> getChildAt(double x, double y) {
        for(GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.isMouseOver(x, y)) {
                return Optional.of(guieventlistener);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        for(GuiEventListener guieventlistener : this.children()) {
            if (guieventlistener.mouseClicked(x, y, button)) {
                this.setFocused(guieventlistener);
                if (button == 0) {
                    this.setDragging(true);
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        this.setDragging(false);
        return this.getChildAt(x, y).filter((child) -> child.mouseReleased(x, y, button)).isPresent();
    }

    public void expand(int width) {}
}
