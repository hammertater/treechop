package ht.treechop.client.gui.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.widget.ToggleWidget;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ToggleGui extends NestedGui {

    private final Widget widget;

    public ToggleGui(Runnable onPress, Supplier<ToggleWidget.State> stateSupplier) {
        this.widget = new ToggleWidget(0, 0, onPress, stateSupplier);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Collections.singletonList(widget);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        widget.x = getBox().getLeft();
        widget.y = getBox().getTop();
        widget.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected int getMinimumWidth() {
        return widget.getWidth();
    }

}
