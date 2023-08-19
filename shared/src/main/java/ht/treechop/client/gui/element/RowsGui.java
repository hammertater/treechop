package ht.treechop.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RowsGui extends NestedGui {

    private final int rowSeparation;
    private final int biggestLeftColumnWidth;
    private final int biggestRightColumnWidth;
    private final List<NestedGui> rows = new ArrayList<>();

    public RowsGui(int rowSeparation, Collection<NestedGui> rows) {
        super(0, 0, 0, 0, Component.empty());
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

    @SuppressWarnings("NullableProblems")
    @Override
    public List<? extends GuiEventListener> children() {
        return rows;
    }

    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        int rowLeft = getBox().getLeft();
        int rowTop = getBox().getTop();
        int rowWidth = getBox().getWidth();

        for (NestedGui row : rows) {
            int rowHeight = row.getMinimumHeight();
            row.setBox(rowLeft, rowTop, rowWidth, rowHeight);
            row.render(gui, mouseX, mouseY, partialTicks);
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
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }
}
