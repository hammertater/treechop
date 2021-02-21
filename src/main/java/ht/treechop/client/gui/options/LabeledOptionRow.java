package ht.treechop.client.gui.options;

import com.mojang.blaze3d.matrix.MatrixStack;
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

    private static final double LABEL_WIDTH_FRACTION = 0.3;
    private final TextWidget label;
    private final OptionRow options;

    public LabeledOptionRow(FontRenderer font, ITextComponent label, OptionRow options) {
        this.label = new TextWidget(0, 0, font, label);
        this.options = options;
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return Stream.of(Collections.singletonList(label), options.getEventListeners())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Override
    public void render(MatrixStack matrixStack, int entryIdx, int top, int left, int width, int height, int mouseX, int mouseY, boolean someBoolean, float partialTicks) {
        int labelWidth = (int) (width * LABEL_WIDTH_FRACTION);
        this.label.x = left;
        this.label.y = top + (height - 8) / 2;
        this.label.render(matrixStack, mouseX, mouseY, partialTicks);
        this.options.render(
                matrixStack,
                entryIdx,
                top,
                left + labelWidth,
                width - labelWidth,
                height,
                mouseX,
                mouseY,
                someBoolean,
                partialTicks
        );
    }

}
