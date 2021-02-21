package ht.treechop.client.gui.options;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.widget.StickyWidget;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExclusiveOptionRow extends OptionRow {

    private List<Widget> widgets;

    protected ExclusiveOptionRow(Collection<Widget> widgets) {
        this.widgets = Lists.newArrayList(widgets);
    }

    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return widgets;
    }

    @Override
    public void render(MatrixStack matrixStack, int entryIdx, int top, int left, int width, int height, int mouseX, int mouseY, boolean someBoolean, float partialTicks) {
        int i = 0;
        for (Widget widget : widgets) {
            widget.x = getButtonX(left, width, i);
            widget.y = top;
            widget.setWidth(getButtonX(left, width, i + 1) - widget.x);
            widget.setHeight(height);
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
            ++i;
        }
    }

    private int getButtonX(int left, int width, int buttonIndex) {
        return left + (int) ((double) width * buttonIndex / widgets.size());
    }

    public static class Builder {
        private final List<Option> options = new LinkedList<>();

        public Builder add(ITextComponent name, Runnable onPress, Supplier<Boolean> stateSupplier) {
            options.add(new Option(name, onPress, stateSupplier));
            return this;
        }

        public ExclusiveOptionRow build() {
            List<Widget> widgets = options.stream()
                    .map(option -> new StickyWidget(0, 0, 0, 0, option.name, option.onPress, option.stateSupplier))
                    .collect(Collectors.toList());
            return new ExclusiveOptionRow(widgets);
        }

        private static class Option {
            private ITextComponent name;
            private Runnable onPress;
            private Supplier<Boolean> stateSupplier;

            public Option(ITextComponent name, Runnable onPress, Supplier<Boolean> stateSupplier) {
                this.name = name;
                this.onPress = onPress;
                this.stateSupplier = stateSupplier;
            }
        }
    }
}
