package ht.treechop.client.gui.element;

import ht.treechop.client.gui.IGuiEventListener;
import ht.treechop.client.gui.util.ScreenBox;
import ht.treechop.client.gui.widget.StickyWidget;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.Collections;
import java.util.List;

public class TextButtonGui extends NestedGui {

    private StickyWidget widget;

    public TextButtonGui(int x, int y, int width, int height, TextComponentTranslation text, Runnable onPress) {
        this.setBox(new ScreenBox(x, y, width, height));
        this.widget = new StickyWidget(0, 0, text, onPress);
    }

    public List<? extends IGuiEventListener> getEventListeners() {
        return Collections.singletonList(widget);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.widget.setBox(getBox());
        this.widget.setBox(getBox());
        this.widget.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public int getMinimumWidth() {
        return Math.max(getBox().getWidth(), widget.getWidth());
    }

    @Override
    public int getMinimumHeight() {
        return Math.max(getBox().getHeight(), widget.getHeight());
    }

}
