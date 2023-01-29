package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.Client;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class ChopIndicator extends GuiComponent {

    private static final double IMAGE_SCALE = 1.0;

    public static void render(PoseStack poseStack, int windowWidth, int windowHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        HitResult mouseOver = minecraft.hitResult;
        Player player = minecraft.player;

        if (Client.isChoppingIndicatorEnabled() &&
                player != null && !player.isSpectator() &&
                minecraft.level != null && minecraft.screen == null && mouseOver != null &&
                mouseOver.getType() == HitResult.Type.BLOCK && mouseOver instanceof BlockHitResult &&
                ChopUtil.playerWantsToChop(player, Client.getChopSettings())
        ) {
            BlockPos blockPos = ((BlockHitResult) mouseOver).getBlockPos();
            if (blockCanBeChopped(blockPos)) {
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
                );
                RenderSystem.setShaderTexture(0, Sprite.TEXTURE_PATH);

                boolean mirror = player.getMainArm() == HumanoidArm.LEFT;
                int indicatorCenterX = windowWidth / 2 + ConfigHandler.CLIENT.indicatorXOffset.get() * (mirror ? -1 : 1);
                int indicatorCenterY = windowHeight / 2 + ConfigHandler.CLIENT.indicatorYOffset.get();

                Sprite sprite = ChopUtil.playerWantsToFell(player, Client.getChopSettings()) ? Sprite.CHOP_INDICATOR : Sprite.NO_FELL_INDICATOR;
                int imageWidth = (int) (sprite.width * IMAGE_SCALE);
                int imageHeight = (int) (sprite.height * IMAGE_SCALE);

                sprite.blit(
                        poseStack,
                        indicatorCenterX - imageWidth / 2,
                        indicatorCenterY - imageHeight / 2,
                        imageWidth,
                        imageHeight,
                        mirror
                );

                RenderSystem.defaultBlendFunc();
            }
        }
    }

    private static boolean blockCanBeChopped(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        ClientChopSettings chopSettings = Client.getChopSettings();

        if (player == null || level == null) {
            return false;
        }

        boolean wantToChop = ChopUtil.canChopWithTool(player, level, pos) && ChopUtil.playerWantsToChop(minecraft.player, chopSettings);
        if (wantToChop) {
            if (ChopUtil.playerWantsToFell(player, chopSettings)) {
                int maxNumTreeBlocks = ConfigHandler.COMMON.maxNumTreeBlocks.get();
                return Client.treeCache.getTree(level, pos, maxNumTreeBlocks).isAProperTree(chopSettings.getTreesMustHaveLeaves());
            } else {
                return ChopUtil.isBlockChoppable(level, pos);
            }
        }

        return false;
    }
}
