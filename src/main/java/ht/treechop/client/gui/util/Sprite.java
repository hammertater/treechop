package ht.treechop.client.gui.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;

public enum Sprite {
    CHOP_INDICATOR(0, 0, 20, 20),
    NO_FELL_INDICATOR(20, 0, 20, 20),
    TOGGLE_BUTTON_OFF(0, 20, 32, 20),
    TOGGLE_BUTTON_ON(32, 20, 32, 20),
    HIGHLIGHTED_TOGGLE_BUTTON_OFF(0, 40, 32, 20),
    HIGHLIGHTED_TOGGLE_BUTTON_ON(32, 40, 32, 20),
    LOCKED_TOGGLE_BUTTON_OFF(0, 60, 32, 20),
    LOCKED_TOGGLE_BUTTON_ON(32, 60, 32, 20),
    PAGE_ONE(0, 80, 32, 20),
    HIGHLIGHTED_PAGE_ONE(32, 80, 32, 20),
    PAGE_TWO(0, 100, 32, 20),
    HIGHLIGHTED_PAGE_TWO(32, 100, 32, 20),
    ;

    public static final ResourceLocation TEXTURE_PATH =
            new ResourceLocation("treechop", "textures/gui/widgets.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 120;

    private final int u;
    private final int v;
    public final int width;
    public final int height;

    Sprite(int u, int v, int width, int height) {
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }

    public static void setRenderState(float alpha) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getTextureManager().bind(Sprite.TEXTURE_PATH);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
    }

    public void blit(MatrixStack matrixStack, int x, int y) {
        blit(matrixStack, x, y, width, height);
    }

    public void blit(MatrixStack matrixStack, int x, int y, double scale) {
        blit(matrixStack, x, y, (int) (width * scale), (int) (height * scale));
    }

    public void blit(MatrixStack matrixStack, int x, int y, int width, int height) {
        AbstractGui.blit(matrixStack, x, y, width, height, u, v, this.width, this.height, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

}
