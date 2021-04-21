package ht.treechop.client.gui.options;

import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractOptionList;

import java.util.Collection;

public class OptionList extends AbstractOptionList<OptionRow> {

    private final int EXCESS_SCROLL = 4; // Vanilla adds this random offset everywhere, it's terrible
    private final int MINIMUM_ROW_WIDTH = 0;

    private final int rowSeparation;
    private final int biggestLeftColumnWidth;
    private final int biggestRightColumnWidth;
    private final int rowWidth;

    public OptionList(Minecraft minecraft, int width, int top, int bottom, int itemHeight, Collection<LabeledOptionRow> rows) {
        super(minecraft, width, top - bottom, top, bottom, itemHeight);
        setBackgroundEnabled(false);
        this.rowSeparation = itemHeight - GUIUtil.BUTTON_HEIGHT;
        setRenderHeader(false, 0);

        rows.forEach(this::addEntry);
        biggestLeftColumnWidth = rows.stream().map(LabeledOptionRow::getLeftColumnWidth).reduce(Integer::max).orElse(0);
        biggestRightColumnWidth = rows.stream().map(LabeledOptionRow::getRightColumnWidth).reduce(Integer::max).orElse(0);
        rows.forEach(row -> row.setColumnWidths(biggestLeftColumnWidth, biggestRightColumnWidth));
        rowWidth = Math.max(MINIMUM_ROW_WIDTH, biggestLeftColumnWidth + biggestRightColumnWidth);
    }

    @Override
    public int getMaxScroll() {
        return getHeightForRows(Math.max(0, getItemCount()), itemHeight) - (y1 - y0) - EXCESS_SCROLL;
    }

    public void setBackgroundEnabled(boolean enabled) {
        // Whether to render list background
        func_244605_b(enabled);

        // Whether to render top and bottom backgrounds
        func_244606_c(enabled);
    }

    public int getRowWidth() {
        return rowWidth;
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }

    @Override
    public double getScrollAmount() {
        return super.getScrollAmount() + EXCESS_SCROLL;
    }

    @Override
    protected int getRowTop(int row) {
        return getTop() + row * itemHeight;
    }

    @Override
    public int getRowLeft() {
        return this.x0 + this.width / 2 - this.getRowWidth() / 2;
    }

    public int getTop() {
        return y0 + headerHeight;
    }

    public int getBottom() {
        return Math.min(y1, getTop() + getItemCount() * itemHeight - rowSeparation);
    }

    static public int getHeightForRows(int numRows, int rowHeight) {
        int rowSeparation = rowHeight - GUIUtil.BUTTON_HEIGHT;
        return numRows * rowHeight - rowSeparation;
    }

}
