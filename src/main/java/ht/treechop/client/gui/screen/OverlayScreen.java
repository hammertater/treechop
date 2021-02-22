package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.util.GUIUtil;

public class OverlayScreen extends ModSettingsScreen {

    private static final int INSET_SIZE = 20;
    private static final boolean IS_PAUSE_SCREEN = true;
    private static final int SPACE_ABOVE_AND_BELOW_LIST = 20;

    public OverlayScreen() {
        super();
    }

    @Override
    public void renderBackground(MatrixStack matrixStack) {
        super.renderBackground(matrixStack);
        fill(matrixStack, INSET_SIZE, INSET_SIZE, width - INSET_SIZE, height - INSET_SIZE, 0x00000080);
    }

    @Override
    protected int getDoneButtonTop() {
        return optionsRowList.getBottom() + SPACE_ABOVE_AND_BELOW_LIST;
    }

    @Override
    protected int getTitleTop() {
        return optionsRowList.getTop() - SPACE_ABOVE_AND_BELOW_LIST - GUIUtil.TEXT_LINE_HEIGHT;
    }

    @Override
    public boolean isPauseScreen() {
        return IS_PAUSE_SCREEN;
    }
}
