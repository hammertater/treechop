package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.IGuiEventListener;
import ht.treechop.client.gui.util.Sprite;

public class SpriteButtonWidget extends Widget implements IGuiEventListener {

    private final Runnable onPress;
    private final Sprite sprite;
    private final Sprite highlightedSprite;

    public SpriteButtonWidget(int x, int y, Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        super(x, y, sprite.width, sprite.height);
        this.onPress = onPress;
        this.sprite = sprite;
        this.highlightedSprite = highlightedSprite;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        Sprite sprite = isHovered() ? highlightedSprite : this.sprite;
        Sprite.setRenderState(1f);
        sprite.blit(getBox().getLeft(), getBox().getTop());
    }

    @Override
    public void onClick(int mouseX, int mouseY, int button) {
        onPress.run();
    }

}