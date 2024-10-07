package ht.treechop.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import ht.treechop.client.gui.screen.ChopIndicator;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Inject(method = "renderCrosshair", at = @At("TAIL"))
    public void injectChopIndicator(GuiGraphics gui, DeltaTracker delta, CallbackInfo info) {
        ChopIndicator.render(gui, gui.guiWidth(), gui.guiHeight());
    }
}
