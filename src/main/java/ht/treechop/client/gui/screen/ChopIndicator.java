package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.Client;
import ht.treechop.client.gui.util.Sprite;
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
        RayTraceResult mouseOver = minecraft.hitResult;

        if (Client.isChoppingIndicatorEnabled() &&
                minecraft.screen == null && mouseOver != null &&
                mouseOver.getType() == RayTraceResult.Type.BLOCK &&
                mouseOver instanceof BlockRayTraceResult
        ) {
            BlockPos blockPos = ((BlockRayTraceResult) mouseOver).getBlockPos();
            if (blockWouldBeChopped(blockPos)) {
                int windowWidth = window.getGuiScaledWidth();
                int windowHeight = window.getGuiScaledHeight();
                minecraft.getTextureManager().bind(Sprite.TEXTURE_PATH);

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
            lastBlockPos = blockPos;
            lastChopSettings.copyFrom(Client.getChopSettings());
        } else {
            lastBlockPos = null;
        }
    }

    private boolean blockWouldBeChopped(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPlayerEntity player = minecraft.player;
        ClientWorld world = minecraft.level;

        if (world != null & minecraft.player != null &&
                ChopUtil.canChopWithTool(player.getMainHandItem()) &&
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
