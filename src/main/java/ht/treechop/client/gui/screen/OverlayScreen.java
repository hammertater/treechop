package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

public class OverlayScreen extends ModSettingsScreen {

    private static final int INSET_SIZE = 20;
    private static final boolean IS_PAUSE_SCREEN = false;

    public OverlayScreen() {
        super();
    }

    @Override
    public void renderBackground(MatrixStack matrixStack) {
        super.renderBackground(matrixStack);
        fill(matrixStack, INSET_SIZE, INSET_SIZE, width - INSET_SIZE, height - INSET_SIZE, 0x00000080);
    }

    @Override
    public boolean isPauseScreen() {
        return IS_PAUSE_SCREEN;
    }
}
