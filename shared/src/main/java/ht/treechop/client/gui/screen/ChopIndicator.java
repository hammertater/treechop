package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.Client;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.util.ChopUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;

public class ChopIndicator extends GuiComponent {

    private static final double IMAGE_SCALE = 1.0;

    private static final LastBlock lastBlock = new LastBlock();
    private static final ChopSettings lastChopSettings = new ChopSettings();
    private static boolean lastBlockWouldBeChopped = false;
    private static boolean wantedToChopLastBlock = false;

    public static void render(PoseStack poseStack, int windowWidth, int windowHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        HitResult mouseOver = minecraft.hitResult;

        if (!ConfigHandler.CLIENT.showChoppingIndicators.get()) {
            return;
        }

        if (Client.isChoppingIndicatorEnabled() &&
                minecraft.level != null && minecraft.screen == null && mouseOver != null &&
                mouseOver.getType() == HitResult.Type.BLOCK && mouseOver instanceof BlockHitResult &&
                ChopUtil.playerWantsToChop(minecraft.player, Client.getChopSettings())
        ) {
            BlockPos blockPos = ((BlockHitResult) mouseOver).getBlockPos();
            if (blockWouldBeChopped(blockPos)) {
                RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                        GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
                );
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

                RenderSystem.defaultBlendFunc();
            }
            lastBlock.set(minecraft.level, blockPos);
            lastChopSettings.copyFrom(Client.getChopSettings());
        } else {
            lastBlock.clear();
        }
    }

    private static boolean blockWouldBeChopped(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        ClientChopSettings chopSettings = Client.getChopSettings();

        if (player == null || level == null) {
            return false;
        }

        boolean wantToChop = ChopUtil.canChopWithTool(player.getMainHandItem()) && ChopUtil.playerWantsToChop(minecraft.player, chopSettings);
        if (wantToChop) {
            if (!lastBlock.equals(level, pos) || !wantedToChopLastBlock) {
                if (ChopUtil.playerWantsToFell(player, chopSettings)) {
                    lastBlockWouldBeChopped = ChopUtil.isPartOfATree(
                            level, pos, chopSettings.getTreesMustHaveLeaves()
                    );
                } else {
                    lastBlockWouldBeChopped = ChopUtil.isBlockALog(level, pos);
                }
            }
        } else {
            lastBlockWouldBeChopped = false;
        }

        wantedToChopLastBlock = wantToChop;
        return lastBlockWouldBeChopped;
    }

    private static class LastBlock {
        private BlockPos pos;
        private BlockState blockState;
        private BlockGetter level;

        public LastBlock() {
            clear();
        }

        public boolean equals(BlockGetter level, BlockPos pos) {
            return (this.pos == null && pos == null) || (this.level == level
                    && this.pos != null
                    && this.pos.equals(pos)
                    && blockState.equals(level.getBlockState(pos))
            );
        }

        public void set(@Nonnull BlockGetter level, @Nonnull BlockPos pos) {
            this.level = level;
            this.pos = pos;
            blockState = level.getBlockState(pos);
        }

        public void clear() {
            pos = null;
            blockState = Blocks.AIR.defaultBlockState();
        }
    }

}
