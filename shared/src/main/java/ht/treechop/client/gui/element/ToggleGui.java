package ht.treechop.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.widget.ToggleWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ToggleGui extends NestedGui {

    private final AbstractWidget widget;
    private final Supplier<Component> tooltipSupplier;

    public ToggleGui(Runnable onPress, Supplier<ToggleWidget.State> stateSupplier, Supplier<Component> componentSupplier) {
        super(0, 0, 0, 0, Component.empty());
        this.widget = new ToggleWidget(0, 0, onPress, stateSupplier);
        this.tooltipSupplier = componentSupplier;
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.singletonList(widget);
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        widget.setX(getBox().getLeft());
        widget.setY(getBox().getTop());
        widget.render(gui, mouseX, mouseY, partialTicks);

        if (widget.isHoveredOrFocused()) {
            GUIUtil.showTooltip(mouseX, mouseY, tooltipSupplier.get());
        }
    }

    @Override
    public int getMinimumWidth() {
        return widget.getWidth();
    }

    @Override
    public int getMinimumHeight() {
        return widget.getHeight();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }
}
