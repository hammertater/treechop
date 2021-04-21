package ht.treechop.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ht.treechop.client.gui.util.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpriteButtonWidget extends Widget {

    private final Runnable onPress;
    private final Sprite sprite;
    private final Sprite highlightedSprite;

    public SpriteButtonWidget(int x, int y, Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        super(x, y, Math.max(sprite.width, highlightedSprite.width), Math.max(sprite.height, highlightedSprite.height), new StringTextComponent(""));
        this.onPress = onPress;
        this.sprite = sprite;
        this.highlightedSprite = highlightedSprite;
        this.active = true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void onClick(double mouseX, double mouseY) {
        onPress.run();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Sprite.setRenderState(this.alpha);
        Sprite sprite = isHovered() ? highlightedSprite : this.sprite;
        sprite.blit(matrixStack, x, y);
    }

}
