package ht.treechop.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.Client;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

public class ChopIndicator extends AbstractGui {

    private static final ResourceLocation TEXTURE_PATH =
            new ResourceLocation("treechop", "textures/gui/widgets.png");
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 64;

    private static final int INDICATOR_X_OFFSET = 16;

    private static final double IMAGE_SCALE = 1.0;
    private static final int IMAGE_UV_WIDTH = 20;
    private static final int IMAGE_UV_HEIGHT = 20;

    private static BlockPos lastBlockPos = null;
    private static boolean lastBlockWouldBeChopped = false;
    private static boolean lastChoppingEnabled = false;
    private static boolean lastFellingEnabled = false;

    public void render(MainWindow window, MatrixStack matrixStack, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        RayTraceResult mouseOver = minecraft.objectMouseOver;

        if (mouseOver != null && mouseOver.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockRayTraceResult) mouseOver).getPos();

            if (Client.getChoppingIndicatorVisible()
                    && ChopUtil.playerWantsToChop(minecraft.player, Client.getChopSettings())
                    && blockWouldBeChopped(blockPos)
            ) {
                int windowWidth = window.getScaledWidth();
                int windowHeight = window.getScaledHeight();
                Minecraft.getInstance().getTextureManager().bindTexture(TEXTURE_PATH);

                int indicatorCenterX = windowWidth / 2 + INDICATOR_X_OFFSET;
                int indicatorCenterY = windowHeight / 2;
                int imageU = ChopUtil.playerWantsToFell(minecraft.player, Client.getChopSettings()) ? 0 : 20;

                final int imageWidth = (int) (IMAGE_UV_WIDTH * IMAGE_SCALE);
                final int imageHeight = (int) (IMAGE_UV_HEIGHT * IMAGE_SCALE);
                blit(
                        matrixStack,
                        indicatorCenterX - imageWidth / 2,
                        indicatorCenterY - imageHeight / 2,
                        imageWidth,
                        imageHeight,
                        imageU,
                        0,
                        IMAGE_UV_WIDTH,
                        IMAGE_UV_HEIGHT,
                        TEXTURE_WIDTH,
                        TEXTURE_HEIGHT
                );
            }
            lastBlockPos = blockPos;
        } else {
            lastBlockPos = null;
        }
    }

    private boolean blockWouldBeChopped(BlockPos pos) {
        ChopSettings chopSettings = Client.getChopSettings();
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerEntity player = minecraft.player;
        ClientWorld world = minecraft.world;

        if (pos != lastBlockPos
                || chopSettings.getChoppingEnabled() != lastChoppingEnabled
                || chopSettings.getFellingEnabled() != lastFellingEnabled) {
            lastBlockPos = pos;
            lastChoppingEnabled = chopSettings.getChoppingEnabled();
            lastFellingEnabled = chopSettings.getFellingEnabled();

            if (ChopUtil.playerWantsToFell(player, Client.getChopSettings()) && ChopUtil.canChopWithTool(player.getHeldItemMainhand())) {
                lastBlockWouldBeChopped = ChopUtil.isPartOfATree(
                        world, pos, Client.getChopSettings().getTreesMustHaveLeaves()
                );
            } else {
                lastBlockWouldBeChopped = ChopUtil.isBlockALog(world, pos);
            }
        }
        return lastBlockWouldBeChopped;
    }

}
