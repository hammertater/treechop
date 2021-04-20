package ht.treechop.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.Client;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

public class ChopIndicator extends AbstractGui {

    private static final double IMAGE_SCALE = 1.0;

    private static BlockPos lastBlockPos = null;
    private static final ChopSettings lastChopSettings = new ChopSettings();
    private static boolean lastBlockWouldBeChopped = false;

    public void render(MainWindow window, MatrixStack matrixStack, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        RayTraceResult mouseOver = minecraft.objectMouseOver;

        if (mouseOver != null && mouseOver.getType() == RayTraceResult.Type.BLOCK && mouseOver instanceof BlockRayTraceResult) {
            BlockPos blockPos = ((BlockRayTraceResult) mouseOver).getPos();
            lastBlockPos = blockPos;

            if (Client.isChoppingIndicatorEnabled()
                    && minecraft.player != null
                    && ChopUtil.playerWantsToChop(minecraft.player, Client.getChopSettings())
                    && blockWouldBeChopped(blockPos)
            ) {
                int windowWidth = window.getScaledWidth();
                int windowHeight = window.getScaledHeight();
                Minecraft.getInstance().getTextureManager().bindTexture(GUIUtil.TEXTURE_PATH);

                int indicatorCenterX = windowWidth / 2 + ConfigHandler.CLIENT.indicatorXOffset.get();
                int indicatorCenterY = windowHeight / 2 + ConfigHandler.CLIENT.indicatorYOffset.get();

                Sprite sprite = ChopUtil.playerWantsToFell(minecraft.player, Client.getChopSettings()) ? Sprite.CHOP_INDICATOR : Sprite.NO_FELL_INDICATOR;
                int imageWidth = (int) (sprite.width * IMAGE_SCALE);
                int imageHeight = (int) (sprite.height * IMAGE_SCALE);
                sprite.blit(
                        matrixStack,
                        indicatorCenterX - imageWidth / 2,
                        indicatorCenterY - imageHeight / 2,
                        imageWidth,
                        imageHeight
                );
            }
        } else {
            lastBlockPos = null;
        }
    }

    private boolean blockWouldBeChopped(BlockPos pos) {
        ChopSettings chopSettings = Client.getChopSettings();
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerEntity player = minecraft.player;
        ClientWorld world = minecraft.world;

        if (world != null && (pos != lastBlockPos
                || !chopSettings.equals(lastChopSettings))) {
            lastBlockPos = pos;
            lastChopSettings.copyFrom(chopSettings);

            if (player != null && ChopUtil.playerWantsToFell(player, Client.getChopSettings()) && ChopUtil.canChopWithTool(player.getHeldItemMainhand())) {
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
