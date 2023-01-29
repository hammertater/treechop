package ht.treechop.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.gui.util.Sprite;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class SpriteButtonWidget extends AbstractWidget {

    private final Runnable onPress;
    private final Sprite sprite;
    private final Sprite highlightedSprite;

    public SpriteButtonWidget(int x, int y, Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        super(x, y, Math.max(sprite.width, highlightedSprite.width), Math.max(sprite.height, highlightedSprite.height), TextComponent.EMPTY);
        this.onPress = onPress;
        this.sprite = sprite;
        this.highlightedSprite = highlightedSprite;
        this.active = true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public void onClick(double mouseX, double mouseY) {
        onPress.run();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Sprite.setRenderState(this.alpha);
        Sprite sprite = isHoveredOrFocused() ? highlightedSprite : this.sprite;
        sprite.blit(poseStack, x, y);
    }

    @Override
    public void updateNarration(NarrationElementOutput out) {
        // TODO
    }
}
