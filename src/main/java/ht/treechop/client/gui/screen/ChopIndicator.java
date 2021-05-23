package ht.treechop.client.gui.screen;

import ht.treechop.client.Client;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class ChopIndicator extends GuiScreen {

    private static final double IMAGE_SCALE = 1.0;

    private static BlockPos lastBlockPos = null;
    private static final ChopSettings lastChopSettings = new ChopSettings();
    private static boolean lastBlockWouldBeChopped = false;

    public void render(float partialTicks, ScaledResolution resolution) {
        Minecraft minecraft = Minecraft.getMinecraft();
        RayTraceResult mouseOver = minecraft.objectMouseOver;

        if (Client.isChoppingIndicatorEnabled() &&
                minecraft.currentScreen == null && mouseOver != null &&
                mouseOver.typeOfHit == RayTraceResult.Type.BLOCK
        ) {
            BlockPos blockPos = mouseOver.getBlockPos();
            if (blockWouldBeChopped(blockPos)) {
                int windowWidth = resolution.getScaledWidth();
                int windowHeight = resolution.getScaledHeight();
                minecraft.getTextureManager().bindTexture(Sprite.TEXTURE_PATH);

                int indicatorCenterX = windowWidth / 2 + ConfigHandler.indicatorXOffset;
                int indicatorCenterY = windowHeight / 2 + ConfigHandler.indicatorYOffset;

                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.enableAlpha();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                Sprite sprite = ChopUtil.playerWantsToFell(minecraft.player, Client.getChopSettings()) ? Sprite.CHOP_INDICATOR : Sprite.NO_FELL_INDICATOR;
                int imageWidth = (int) (sprite.width * IMAGE_SCALE);
                int imageHeight = (int) (sprite.height * IMAGE_SCALE);
                sprite.blit(
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
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        WorldClient world = minecraft.world;

        if (world != null & minecraft.player != null &&
                ChopUtil.canChopWithTool(player.getHeldItemMainhand()) &&
                ChopUtil.playerWantsToChop(minecraft.player, Client.getChopSettings())
        ) {
            if ((pos.equals(lastBlockPos) || !Client.getChopSettings().equals(lastChopSettings))
            ) {
                if (ChopUtil.playerWantsToFell(player, Client.getChopSettings())) {
                    lastBlockWouldBeChopped = ChopUtil.isPartOfATree(
                            world, pos, Client.getChopSettings().getTreesMustHaveLeaves()
                    );
                } else {
                    lastBlockWouldBeChopped = ChopUtil.isBlockALog(world, pos);
                }
            }
        } else {
            lastBlockWouldBeChopped = false;
        }

        return lastBlockWouldBeChopped;
    }

}
