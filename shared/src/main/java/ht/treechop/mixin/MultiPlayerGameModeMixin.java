package ht.treechop.mixin;

import ht.treechop.TreeChopException;
import ht.treechop.api.TreeData;
import ht.treechop.client.Client;
import ht.treechop.client.gui.screen.ChopIndicator;
import ht.treechop.common.chop.ChopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Shadow(remap = false) @Final private Minecraft minecraft;

    @Shadow(remap = false) public abstract boolean isServerControlledInventory();

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true, remap = false)
    public void dontPredictBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        try {
            Player player = minecraft.player;
            Level level = minecraft.level;
            if (player != null && level != null) {
                if (ChopUtil.playerWantsToChop(player, Client.getChopSettings()) && ChopIndicator.blockCanBeChopped(pos)) {
                    TreeData tree = Client.treeCache.getTree(level, pos);
                    BlockState state = level.getBlockState(pos);
                    boolean felled = tree.readyToFell(tree.getChops() + ChopUtil.getNumChopsByTool(player.getMainHandItem(), state));

                    if (!felled) {
                        ChopUtil.thwack(player, level, pos, state);
                        info.setReturnValue(false);
                    }
                }
            }
        } catch (TreeChopException e) {
            // Ignore
        }
    }
}
