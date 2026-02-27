package ht.treechop.client.gui.util;

import com.mojang.blaze3d.systems.RenderSystem;
import ht.treechop.TreeChop;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

import java.util.function.Function;

public enum Sprite {
    CHOP_INDICATOR(TreeChop.resource("widget/chop_indicator"), 20, 20),
    TOGGLE_BUTTON_OFF(TreeChop.resource("widget/toggle_off"), 32, 20),
    TOGGLE_BUTTON_ON(TreeChop.resource("widget/toggle_on"), 32, 20),
    HIGHLIGHTED_TOGGLE_BUTTON_OFF(TreeChop.resource("widget/toggle_off_highlighted"), 32, 20),
    HIGHLIGHTED_TOGGLE_BUTTON_ON(TreeChop.resource("widget/toggle_on_highlighted"), 32, 20),
    LOCKED_TOGGLE_BUTTON_OFF(TreeChop.resource("widget/toggle_off_locked"), 32, 20),
    LOCKED_TOGGLE_BUTTON_ON(TreeChop.resource("widget/toggle_on_locked"), 32, 20),
    PAGE_ONE(TreeChop.resource("widget/page_one"), 32, 20),
    HIGHLIGHTED_PAGE_ONE(TreeChop.resource("widget/page_one_highlighted"), 32, 20),
    PAGE_TWO(TreeChop.resource("widget/page_two"), 32, 20),
    HIGHLIGHTED_PAGE_TWO(TreeChop.resource("widget/page_two_highlighted"), 32, 20),
    ;

    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 120;

    private final ResourceLocation texture;
    public final int width;
    public final int height;

    Sprite(ResourceLocation texture, int width, int height) {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public void setRenderState(float alpha) {
        RenderSystem.setShader(CoreShaders.POSITION_TEX);
        RenderSystem.setShaderTexture(0, this.texture);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
    }

    public void blit(Function<ResourceLocation, RenderType> renderType, GuiGraphics gui, int x, int y, float alpha) {
        gui.blitSprite(renderType, this.texture, x, y, this.width, this.height, ARGB.white(alpha));
    }

    public void blitMirror(Function<ResourceLocation, RenderType> renderType, GuiGraphics gui, int x, int y, float alpha, float scale, boolean mirror) {
        int mx = mirror ? x + (int) (this.width * scale) : x;
        int mw = (int) ((mirror ? -this.width : this.width) * scale);
        gui.blitSprite(renderType, this.texture, mx, y, mw, (int) (this.height * scale), ARGB.white(alpha));
    }

}
