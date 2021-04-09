package ht.treechop.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.options.OptionList;
import ht.treechop.client.gui.util.GUIUtil;

public class InGameSettingsScreen extends ClientSettingsScreen {

    private static final int INSET_SIZE = 20;
    private static final boolean IS_PAUSE_SCREEN = true;
    private static final int SPACE_ABOVE_AND_BELOW_LIST = 20;

    public InGameSettingsScreen() {
        super();
    }

    @Override
    public void renderBackground(MatrixStack matrixStack) {
        super.renderBackground(matrixStack);
        fill(matrixStack, INSET_SIZE, INSET_SIZE, width - INSET_SIZE, height - INSET_SIZE, 0x00000080);
    }

    @Override
    protected int getTop() {
        return getTitleTop();
    }

    @Override
    protected int getTitleTop() {
        return getListTop() - SPACE_ABOVE_AND_BELOW_LIST - GUIUtil.TEXT_LINE_HEIGHT;
    }

    @Override
    protected int getListTop() {
        return height / 2 - getListHeight() / 2;
    }

    @Override
    protected int getListBottom() {
        return height / 2 + getListHeight() / 2;
    }

    private int getListHeight() {
        return OptionList.getHeightForRows(5, ROW_HEIGHT);
    }

    @Override
    protected int getDoneButtonTop() {
        return getListBottom() + SPACE_ABOVE_AND_BELOW_LIST;
    }

    @Override
    protected int getBottom() {
        return getDoneButtonTop() + GUIUtil.BUTTON_HEIGHT;
    }

    @Override
    public boolean isPauseScreen() {
        return IS_PAUSE_SCREEN;
    }
}
