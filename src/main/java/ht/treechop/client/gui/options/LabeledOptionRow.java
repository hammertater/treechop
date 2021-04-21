package ht.treechop.client.gui.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.widget.TextWidget;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.util.text.ITextComponent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LabeledOptionRow extends OptionRow {

    private static final int COLUMN_PADDING = 4;

    private final TextWidget label;
    private final OptionRow options;
    private int leftColumnWidth;
    private int rightcolumnWidth;
    private boolean rightAlignLabels = false;

    public LabeledOptionRow(FontRenderer font, ITextComponent label, OptionRow options) {
        this.label = new TextWidget(0, 0, font, label);
        this.options = options;
        this.leftColumnWidth = getLeftColumnWidth();
        this.rightcolumnWidth = getRightColumnWidth();
    }

    public int getMinimumWidth() {
        return getLeftColumnWidth() + options.getMinimumWidth();
    }

    public int getLeftColumnWidth() {
        return label.getWidth() + COLUMN_PADDING;
    }

    public int getRightColumnWidth() {
        return options.getMinimumWidth() + COLUMN_PADDING;
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Stream.of(Collections.singletonList(label), options.getEventListeners())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void resize(int width) {
    }

    @Override
    public void render(MatrixStack matrixStack, int entryIdx, int top, int left, int width, int height, int mouseX, int mouseY, boolean someBoolean, float partialTicks) {
        int center = left + leftColumnWidth;
        this.label.x = center - COLUMN_PADDING + (rightAlignLabels ? 0 : -leftColumnWidth);
        this.label.y = top + (height - 6) / 2;
        this.label.render(matrixStack, mouseX, mouseY, partialTicks, rightAlignLabels);
        this.options.render(
                matrixStack,
                entryIdx,
                top,
                center + COLUMN_PADDING,
                rightcolumnWidth,
                height,
                mouseX,
                mouseY,
                someBoolean,
                partialTicks
        );
    }

    public void setColumnWidths(int biggestLeftColumnWidth, int biggestRightColumnWidth) {
        this.leftColumnWidth = biggestLeftColumnWidth;
        this.rightcolumnWidth = biggestRightColumnWidth;
        options.resize(biggestRightColumnWidth);
    }
}
