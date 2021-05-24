package ht.treechop.client.gui.element;

import ht.treechop.client.gui.IGuiEventListener;
import ht.treechop.client.gui.widget.TextWidget;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LabeledGui extends NestedGui {

    private static final int COLUMN_PADDING = 4;

    private final TextWidget label;
    private final NestedGui gui;
    private int leftColumnWidth;
    private int rightcolumnWidth;
    private boolean rightAlignLabels = false;

    public LabeledGui(FontRenderer font, ITextComponent label, NestedGui gui) {
        this.label = new TextWidget(0, 0, font, label);
        this.gui = gui;
        this.leftColumnWidth = getLeftColumnWidth();
        this.rightcolumnWidth = getRightColumnWidth();
    }

    public int getMinimumWidth() {
        return getLeftColumnWidth() + gui.getMinimumWidth();
    }

    public int getMinimumHeight() {
        return Math.max(label.getHeight(), gui.getMinimumHeight());
    }

    public int getLeftColumnWidth() {
        return label.getWidth() + COLUMN_PADDING;
    }

    public int getRightColumnWidth() {
        return gui.getMinimumWidth() + COLUMN_PADDING;
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Stream.of(Collections.singletonList(label), gui.getEventListeners())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        int center = getBox().getCenterX() + (leftColumnWidth - rightcolumnWidth) / 2;
        this.label.getBox().setLeft(center - COLUMN_PADDING + (rightAlignLabels ? 0 : -leftColumnWidth));
        this.label.getBox().setTop(getBox().getCenterY() - 3);
        this.label.render(mouseX, mouseY, partialTicks, rightAlignLabels);
        this.gui.setBox(center + COLUMN_PADDING, getBox().getTop(), rightcolumnWidth, getBox().getHeight());
        this.gui.render(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    public void setColumnWidths(int biggestLeftColumnWidth, int biggestRightColumnWidth) {
        this.leftColumnWidth = biggestLeftColumnWidth;
        this.rightcolumnWidth = biggestRightColumnWidth;
//        element.resize(biggestRightColumnWidth);
    }
}
