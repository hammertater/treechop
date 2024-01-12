package ht.treechop.mixin;

import ht.treechop.TreeChopException;
import ht.treechop.client.Client;
import ht.treechop.client.gui.screen.ChopIndicator;
import ht.treechop.common.chop.ChopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow @Final private Minecraft minecraft;

    @Shadow public abstract boolean isServerControlledInventory();

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    public void dontPredictBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        try {
            if (minecraft.player != null) {
                if (ChopUtil.playerWantsToChop(minecraft.player, Client.getChopSettings()) && ChopIndicator.blockCanBeChopped(pos)) {
                    ChopUtil.thwack(minecraft.player, minecraft.level, pos, minecraft.level.getBlockState(pos));
                    info.setReturnValue(false);
                }
            }
        } catch (TreeChopException e) {
            // Ignore
        }
    }
}
