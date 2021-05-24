package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

public class StickyWidget extends Widget {

    protected static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");

    private final Supplier<State> stateSupplier;
    private final Runnable onPress;
    private final ITextComponent text;

    public StickyWidget(int x, int y, int width, int height, ITextComponent text, Runnable onPress, Supplier<State> stateSupplier) {
        super(x, y, Math.max(width, GUIUtil.getMinimumButtonWidth(text)), Math.max(height, GUIUtil.BUTTON_HEIGHT));
        this.onPress = onPress;
        this.stateSupplier = stateSupplier;
        this.text = text;
    }

    public StickyWidget(int x, int y, ITextComponent text, Runnable onPress) {
        super(x, y, Math.max(Minecraft.getMinecraft().fontRenderer.getStringWidth(text.getUnformattedText()), GUIUtil.getMinimumButtonWidth(text)), GUIUtil.BUTTON_HEIGHT);
        this.onPress = onPress;
        this.stateSupplier = () -> State.Up;
        this.text = text;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        this.active = stateSupplier.get() == State.Up;
        getBox().setHeight(Math.min(getHeight(), GUIUtil.BUTTON_HEIGHT));
        renderButton(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClick(int mouseX, int mouseY, int button) {
        onPress.run();
    }

    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        FontRenderer fontrenderer = minecraft.fontRenderer;
        minecraft.getTextureManager().bindTexture(WIDGETS_LOCATION);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        int x = getBox().getLeft();
        int y = getBox().getTop();
        int width = getWidth();
        int height = getHeight();

        if (stateSupplier.get() != State.Locked) {
            int i = this.getYImage(this.isHovered());
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableDepth();

            this.blit(x, y, 0, 46 + i * 20, width / 2, height);
            this.blit(x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
        }

        drawString(text, x + (width - fontrenderer.getStringWidth(text.getUnformattedText())) / 2, y + (height - 8) / 2, getFGColor());
    }

    private int getFGColor() {
        return this.active ? 16777215 : 10526880; // White : Light Grey
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
