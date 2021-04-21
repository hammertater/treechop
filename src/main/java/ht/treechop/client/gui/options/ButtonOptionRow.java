package ht.treechop.client.gui.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.SpriteButtonWidget;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.Collections;
import java.util.List;

public class ButtonOptionRow extends OptionRow {

    private final SpriteButtonWidget widget;

    public ButtonOptionRow(Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        this.widget = new SpriteButtonWidget(0, 0, sprite, highlightedSprite, onPress);
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Collections.singletonList(widget);
    }

    @Override
    public void resize(int width) {
    }

    @Override
    public void render(MatrixStack matrixStack, int entryIdx, int top, int left, int width, int height, int mouseX, int mouseY, boolean someBoolean, float partialTicks) {
        this.widget.x = left + width / 2 - this.widget.getWidth() / 2;
        this.widget.y = top + height / 2 - this.widget.getHeightRealms() / 2;
        this.widget.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected int getMinimumWidth() {
        return widget.getWidth();
    }

}
