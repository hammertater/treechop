package ht.treechop.client.gui.options;

import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractOptionList;

import java.util.Collection;

public class OptionList extends AbstractOptionList<OptionRow> {

    private final int rowSeparation;
    private final int biggestLeftColumnWidth;
    private final int biggestRightColumnWidth;

    private int rowWidth = 200;

    public OptionList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight, Collection<LabeledOptionRow> rows) {
        super(minecraft, width, height, top, bottom, itemHeight);
        setBackgroundEnabled(false);
        this.rowSeparation = itemHeight - GUIUtil.BUTTON_HEIGHT;

        int frameHeight = bottom - top;
        int listHeight = rows.size() * itemHeight - rowSeparation;
        headerHeight = Math.max(0, (frameHeight - listHeight) / 2);

        rows.forEach(this::addEntry);
        biggestLeftColumnWidth = rows.stream().map(LabeledOptionRow::getLeftColumnWidth).reduce(Integer::max).orElse(0);
        biggestRightColumnWidth = rows.stream().map(LabeledOptionRow::getRightColumnWidth).reduce(Integer::max).orElse(0);
        rows.forEach(row -> row.setColumnWidths(biggestLeftColumnWidth, biggestRightColumnWidth));
        rowWidth = Math.max(rowWidth, biggestLeftColumnWidth + biggestRightColumnWidth);
    }

    @Override
    public double getScrollAmount() {
        return super.getScrollAmount() + 4;
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

    protected int getRowTop(int row) {
        return getTop() + row * itemHeight;
    }

    public int getTop() {
        return y0 + headerHeight;
    }

    public int getBottom() {
        return getTop() + getItemCount() * itemHeight - rowSeparation;
    }

}
