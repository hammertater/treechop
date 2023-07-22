package ht.treechop.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.SpriteButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ButtonGui extends NestedGui {

    private final SpriteButtonWidget widget;

    public ButtonGui(Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        super(0, 0, 0, 0, Component.empty());
        this.widget = new SpriteButtonWidget(0, 0, sprite, highlightedSprite, onPress);
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return Collections.singletonList(widget);
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.widget.setX(getBox().getCenterX() - this.widget.getWidth() / 2);
        this.widget.setY(getBox().getCenterY() - this.widget.getHeight() / 2);
        this.widget.render(gui, mouseX, mouseY, partialTicks);
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
