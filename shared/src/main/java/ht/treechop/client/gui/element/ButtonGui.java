package ht.treechop.client.gui.element;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.SpriteButtonWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.TextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ButtonGui extends NestedGui {

    private final SpriteButtonWidget widget;

    public ButtonGui(Sprite sprite, Sprite highlightedSprite, Runnable onPress) {
        super(0, 0, 0, 0, TextComponent.EMPTY);
        this.widget = new SpriteButtonWidget(0, 0, sprite, highlightedSprite, onPress);
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return Collections.singletonList(widget);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.widget.x = getBox().getCenterX() - this.widget.getWidth() / 2;
        this.widget.y = getBox().getCenterY() - this.widget.getHeight() / 2;
        this.widget.render(poseStack, mouseX, mouseY, partialTicks);
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
