package ht.treechop.client.gui.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.widget.StickyWidget;
import ht.treechop.client.gui.widget.ToggleWidget;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ToggleOptionRow extends OptionRow {

    private final Widget widget;

    public ToggleOptionRow(Runnable onPress, Supplier<ToggleWidget.State> stateSupplier) {
        this.widget = new ToggleWidget(0, 0, onPress, stateSupplier);
    }

    @Override
    public void resize(int width) {
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Collections.singletonList(widget);
    }

    @Override
    public void render(MatrixStack matrixStack, int entryIdx, int top, int left, int width, int height, int mouseX, int mouseY, boolean someBoolean, float partialTicks) {
        widget.x = left;
        widget.y = top;
        widget.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected int getMinimumWidth() {
        return widget.getWidth();
    }

}
