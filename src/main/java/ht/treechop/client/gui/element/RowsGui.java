package ht.treechop.client.gui.element;

import ht.treechop.client.gui.IGuiEventListener;
import ht.treechop.client.gui.util.ScreenBox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RowsGui extends NestedGui {

    private final int rowSeparation;
    private final int biggestLeftColumnWidth;
    private final int biggestRightColumnWidth;
    private final List<NestedGui> rows = new ArrayList<>();
    private ScreenBox box;

    public RowsGui(int rowSeparation, Collection<NestedGui> rows) {
        this.rowSeparation = rowSeparation;
        this.rows.addAll(rows);

        biggestLeftColumnWidth = rows.stream().map(NestedGui::getLeftColumnWidth).reduce(Integer::max).orElse(0);
        biggestRightColumnWidth = rows.stream().map(NestedGui::getRightColumnWidth).reduce(Integer::max).orElse(0);
        rows.forEach(row -> row.setColumnWidths(biggestLeftColumnWidth, biggestRightColumnWidth));
    }

    public int getHeight() {
        return getHeightForRows(rows, rowSeparation);
    }

    public static int getHeightForRows(Collection<NestedGui> rows, int rowSeparation) {
        return Math.max(0, rows.stream().map(NestedGui::getMinimumHeight).reduce(Integer::sum).orElse(0) + (rows.size() - 1) * rowSeparation);
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return rows;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        int rowLeft = getBox().getLeft();
        int rowTop = getBox().getTop();
        int rowWidth = getBox().getWidth();

        for (NestedGui row : rows) {
            int rowHeight = row.getMinimumHeight();
            row.setBox(rowLeft, rowTop, rowWidth, rowHeight);
            row.render(mouseX, mouseY, partialTicks);
            rowTop += rowHeight + rowSeparation;
        }
    }

    @Override
    public int getMinimumWidth() {
        return 0;
    }

    @Override
    public int getMinimumHeight() {
        return 0;
    }

    @Override
    public ScreenBox getBox() {
        return this.box;
    }

    @Override
    public void setBox(ScreenBox box) {
        this.box = box;
    }

}
