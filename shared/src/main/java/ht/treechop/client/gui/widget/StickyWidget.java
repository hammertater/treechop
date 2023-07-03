package ht.treechop.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

public class StickyWidget extends AbstractWidget {

    private final Supplier<State> stateSupplier;
    private final Runnable onPress;

    public StickyWidget(int x, int y, int width, int height, Component name, Runnable onPress, Supplier<State> stateSupplier) {
        super(x, y, Math.max(width, GUIUtil.getMinimumButtonWidth(name)), Math.max(height, GUIUtil.BUTTON_HEIGHT), name);
        this.onPress = onPress;
        this.stateSupplier = stateSupplier;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        this.active = stateSupplier.get() == State.Up;
        this.height = Math.min(this.height, GUIUtil.BUTTON_HEIGHT);
        super.render(poseStack, mouseX, mouseY, partialTicks);
    }

    public void onClick(double mouseX, double mouseY) {
        onPress.run();
    }

    // Taken from Forge's AbstractWidget
    public int getFGColor() {
        return this.active ? 16777215 : 10526880; // White : Light Grey
    }

    @Override
    public void renderWidget(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

        if (stateSupplier.get() != State.Locked) {
            int i = this.getYImage(this.isHoveredOrFocused());
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            blit(poseStack, getX(), getY(), 0, 46 + i * 20, this.width / 2, this.height);
            blit(poseStack, getX() + this.width / 2, getY(), 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        }

        int j = getFGColor();
        drawCenteredString(poseStack, font, this.getMessage(), getX() + this.width / 2, getY() + (this.height - 8) / 2, j | (int)Math.ceil(this.alpha * 255.0F) << 24);
    }

    private int getYImage(boolean hoveredOrFocused) {
        return active ? (hoveredOrFocused ? 2 : 1) : 0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }

    public enum State {
        Up,
        Down,
        Locked;

        public static State of(boolean enabled, boolean canBeEnabled) {
            if (canBeEnabled) {
                return enabled ? Down : Up;
            } else {
                return State.Locked;
            }
        }
    }

}
