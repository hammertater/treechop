package ht.treechop.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import ht.treechop.client.gui.util.Sprite;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

public class SpriteButtonWidget extends AbstractWidget {

    private final Runnable onPress;
    private final Sprite sprite;
    private final Sprite highlightedSprite;

    public SpriteButtonWidget(int x, int y, Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        super(x, y, Math.max(sprite.width, highlightedSprite.width), Math.max(sprite.height, highlightedSprite.height), Component.empty());
        this.onPress = onPress;
        this.sprite = sprite;
        this.highlightedSprite = highlightedSprite;
        this.active = true;
    }

    public void onClick(double mouseX, double mouseY) {
        onPress.run();
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        Sprite sprite = isHoveredOrFocused() ? highlightedSprite : this.sprite;
        sprite.blit(RenderType::guiTextured, gui, getX(), getY(), this.alpha);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }
}
