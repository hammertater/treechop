package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.Client;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ChopIndicator extends GuiComponent {

    private static final double IMAGE_SCALE = 1.0;

    private static BlockPos lastBlockPos = null;
    private static final ChopSettings lastChopSettings = new ChopSettings();
    private static boolean lastBlockWouldBeChopped = false;

    public void render(Window window, PoseStack poseStack, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        HitResult mouseOver = minecraft.hitResult;

        if (Client.isChoppingIndicatorEnabled() &&
                minecraft.screen == null && mouseOver != null &&
                mouseOver.getType() == HitResult.Type.BLOCK &&
                mouseOver instanceof BlockHitResult
        ) {
            BlockPos blockPos = ((BlockHitResult) mouseOver).getBlockPos();
            if (blockWouldBeChopped(blockPos)) {
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                int windowWidth = window.getGuiScaledWidth();
                int windowHeight = window.getGuiScaledHeight();
                RenderSystem.setShaderTexture(0, Sprite.TEXTURE_PATH);

                int indicatorCenterX = windowWidth / 2 + ConfigHandler.CLIENT.indicatorXOffset.get();
                int indicatorCenterY = windowHeight / 2 + ConfigHandler.CLIENT.indicatorYOffset.get();

                Sprite sprite = ChopUtil.playerWantsToFell(minecraft.player, Client.getChopSettings()) ? Sprite.CHOP_INDICATOR : Sprite.NO_FELL_INDICATOR;
                int imageWidth = (int) (sprite.width * IMAGE_SCALE);
                int imageHeight = (int) (sprite.height * IMAGE_SCALE);
                sprite.blit(
                        poseStack,
                        indicatorCenterX - imageWidth / 2,
                        indicatorCenterY - imageHeight / 2,
                        imageWidth,
                        imageHeight
                );
            }
            lastBlockPos = blockPos;
            lastChopSettings.copyFrom(Client.getChopSettings());
        } else {
            lastBlockPos = null;
        }
    }

    private boolean blockWouldBeChopped(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;

        if (level != null & minecraft.player != null &&
                ChopUtil.canChopWithTool(player.getMainHandItem()) &&
                ChopUtil.playerWantsToChop(minecraft.player, Client.getChopSettings())
        ) {
            if ((pos.equals(lastBlockPos) || !Client.getChopSettings().equals(lastChopSettings))
            ) {
                if (ChopUtil.playerWantsToFell(player, Client.getChopSettings())) {
                    lastBlockWouldBeChopped = ChopUtil.isPartOfATree(
                            level, pos, Client.getChopSettings().getTreesMustHaveLeaves()
                    );
                } else {
                    lastBlockWouldBeChopped = ChopUtil.isBlockALog(level, pos);
                }
            }
        } else {
            lastBlockWouldBeChopped = false;
        }

        return lastBlockWouldBeChopped;
    }

}
