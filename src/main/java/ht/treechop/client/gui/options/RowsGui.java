package ht.treechop.client.gui.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.util.IPositionalGui;
import ht.treechop.client.gui.util.ScreenBox;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RowsGui extends FocusableGui implements IPositionalGui {

    private final int MINIMUM_ROW_WIDTH = 0;

    private final int rowSeparation;
    private final int biggestLeftColumnWidth;
    private final int biggestRightColumnWidth;
    private final int rowWidth;
    private final int rowHeight;
    private final List<NestedGui> rows = new ArrayList<>();
    private ScreenBox box;

    public RowsGui(Minecraft minecraft, int rowHeight, Collection<NestedGui> rows) {
        this.rowSeparation = rowHeight - GUIUtil.BUTTON_HEIGHT;
        this.rows.addAll(rows);

        biggestLeftColumnWidth = rows.stream().map(NestedGui::getLeftColumnWidth).reduce(Integer::max).orElse(0);
        biggestRightColumnWidth = rows.stream().map(NestedGui::getRightColumnWidth).reduce(Integer::max).orElse(0);
        rows.forEach(row -> row.setColumnWidths(biggestLeftColumnWidth, biggestRightColumnWidth));
        rowWidth = Math.max(MINIMUM_ROW_WIDTH, biggestLeftColumnWidth + biggestRightColumnWidth);
        this.rowHeight = rowHeight;
    }

    public int getRowWidth() {
        return rowWidth;
    }

    public int getHeight() {
        return rows.size() * rowHeight - rowSeparation;
    }

    static public int getHeightForRows(int numRows, int rowHeight) {
        int rowSeparation = rowHeight - GUIUtil.BUTTON_HEIGHT;
        return numRows * rowHeight - rowSeparation;
    }

    public int getNumRows() {
        return rows.size();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return rows;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int rowLeft = getBox().getLeft();
        int rowTop = getBox().getTop();
        int rowWidth = getBox().getWidth();

        for (NestedGui row : rows) {
            row.setBox(rowLeft, rowTop, rowWidth, rowHeight);
            row.render(matrixStack, mouseX, mouseY, partialTicks);
            rowTop += rowHeight + rowSeparation;
        }
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
