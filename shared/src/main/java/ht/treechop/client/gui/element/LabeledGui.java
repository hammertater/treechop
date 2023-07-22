package ht.treechop.client.gui.element;

import net.minecraft.client.gui.GuiGraphics;
import ht.treechop.client.gui.widget.TextWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

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

    public LabeledGui(Font font, Component label, NestedGui gui) {
        super(0, 0, 0, 0, label);
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
    public List<? extends GuiEventListener> children() {
        return Stream.of(Collections.singletonList(label), gui.children())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        int center = getBox().getCenterX() + (leftColumnWidth - rightcolumnWidth) / 2;
        this.label.setX(center - COLUMN_PADDING + (rightAlignLabels ? 0 : -leftColumnWidth));
        this.label.setY(getBox().getCenterY() - 3);
        this.label.render(gui, mouseX, mouseY, partialTicks);
        this.gui.setBox(center + COLUMN_PADDING, getBox().getTop(), rightcolumnWidth, getBox().getHeight());
        this.gui.render(gui, mouseX, mouseY, partialTicks);
    }

    public void setColumnWidths(int biggestLeftColumnWidth, int biggestRightColumnWidth) {
        this.leftColumnWidth = biggestLeftColumnWidth;
        this.rightcolumnWidth = biggestRightColumnWidth;
        gui.expand(biggestRightColumnWidth);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }
}
