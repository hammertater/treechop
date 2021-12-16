package ht.treechop.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.gui.widget.ToggleWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ToggleGui extends NestedGui {

    private final AbstractWidget widget;

    public ToggleGui(Runnable onPress, Supplier<ToggleWidget.State> stateSupplier) {
        super(0, 0, 0, 0, TextComponent.EMPTY);
        this.widget = new ToggleWidget(0, 0, onPress, stateSupplier);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return Collections.singletonList(widget);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        widget.x = getBox().getLeft();
        widget.y = getBox().getTop();
        widget.render(poseStack, mouseX, mouseY, partialTicks);
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
    public void updateNarration(NarrationElementOutput p_169152_) {
        // TODO
    }
}
