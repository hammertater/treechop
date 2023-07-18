package ht.treechop.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.gui.screen.ChopIndicator;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Shadow
    private int screenWidth;

    @Shadow
    private int screenHeight;

    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    public void injectChopIndicator(PoseStack poseStack, CallbackInfo info) {
        ChopIndicator.render(poseStack, screenWidth, screenHeight);
    }
}
