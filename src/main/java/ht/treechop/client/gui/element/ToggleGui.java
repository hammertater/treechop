package ht.treechop.client.gui.element;

import ht.treechop.client.gui.IGuiEventListener;
import ht.treechop.client.gui.widget.ToggleWidget;
import ht.treechop.client.gui.widget.Widget;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ToggleGui extends NestedGui {

    private final Widget widget;

    public ToggleGui(Runnable onPress, Supplier<ToggleWidget.State> stateSupplier) {
        this.widget = new ToggleWidget(0, 0, onPress, stateSupplier);
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Collections.singletonList(widget);
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        widget.getBox().setLeft(getBox().getLeft());
        widget.getBox().setTop(getBox().getTop());
        widget.render(mouseX, mouseY, partialTicks);
    }

    public int getMinimumWidth() {
        return widget.getWidth();
    }

    public int getMinimumHeight() {
        return widget.getHeight();
    }

}
