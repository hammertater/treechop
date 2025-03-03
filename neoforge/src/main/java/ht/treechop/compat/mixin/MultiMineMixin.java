package ht.treechop.compat.mixin;

import atomicstryker.multimine.client.MultiMineClient;
import ht.treechop.client.Client;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiMineClient.class)
public class MultiMineMixin {
    @Inject(method = "onClickBlock", at = @At("HEAD"), cancellable = true)
    private static void injectOnClickBlock(PlayerInteractEvent.LeftClickBlock event, CallbackInfo ci) {
        if (ConfigHandler.COMMON.compatForMultiMine.get() && ChopUtil.isBlockChoppable(event.getLevel(), event.getPos()) && ChopUtil.playerWantsToChop(event.getEntity(), Client.getChopSettings())) {
            ci.cancel();
        }
    }
}
