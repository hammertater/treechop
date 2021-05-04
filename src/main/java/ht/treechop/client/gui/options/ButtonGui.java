package ht.treechop.client.gui.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.SpriteButtonWidget;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.Collections;
import java.util.List;

public class ButtonGui extends NestedGui {

    private final SpriteButtonWidget widget;

    public ButtonGui(Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        this.widget = new SpriteButtonWidget(0, 0, sprite, highlightedSprite, onPress);
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Collections.singletonList(widget);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.widget.x = getBox().getCenterX() - this.widget.getWidth() / 2;
        this.widget.y = getBox().getCenterY() - this.widget.getHeightRealms() / 2;
        this.widget.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    protected int getMinimumWidth() {
        return widget.getWidth();
    }

}
