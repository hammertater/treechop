package ht.treechop.client.gui.element;

import ht.treechop.client.gui.IGuiEventListener;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.SpriteButtonWidget;

import java.util.Collections;
import java.util.List;

public class ButtonGui extends NestedGui {

    private final SpriteButtonWidget widget;

    public ButtonGui(Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        this.widget = new SpriteButtonWidget(0, 0, sprite, highlightedSprite, onPress);
    }

    public List<? extends IGuiEventListener> getEventListeners() {
        return Collections.singletonList(widget);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.widget.getBox().setLeft(getBox().getCenterX() - this.widget.getWidth() / 2);
        this.widget.getBox().setTop(getBox().getCenterY() - this.widget.getHeight() / 2);
        this.widget.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public int getMinimumWidth() {
        return widget.getWidth();
    }

    @Override
    public int getMinimumHeight() {
        return widget.getHeight();
    }

}
