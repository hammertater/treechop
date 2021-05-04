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

public class ExclusiveButtonsGui extends NestedGui {

    private final List<Widget> widgets;

    protected ExclusiveButtonsGui(Collection<Widget> widgets) {
        this.widgets = Lists.newArrayList(widgets);
    }

    public void resize(int width) {
        if (getMinimumWidth() < width) {
            int targetWidth = width / widgets.size();
            List<Widget> smallerWidgets = widgets.stream().filter(widget -> widget.getWidth() <= targetWidth).collect(Collectors.toList());
            List<Widget> biggerWidgets = widgets.stream().filter(widget -> widget.getWidth() > targetWidth).collect(Collectors.toList());
            int totalWidthForSmallers = width - biggerWidgets.stream().map(Widget::getWidth).reduce(Integer::sum).orElse(0);
            int i = 0;
            // Do this incrementally to account for rounding errors
            for (Widget widget : smallerWidgets) {
                double lower = (double) i / smallerWidgets.size();
                double upper = (double) (i + 1) / smallerWidgets.size();
                int widgetWidth = (int) (totalWidthForSmallers * (upper - lower));
                widget.setWidth(widgetWidth);
                ++i;
            }
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<? extends IGuiEventListener> getEventListeners() {
        return widgets;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        int x = getBox().getLeft();
        int y = getBox().getTop();
        for (Widget widget : widgets) {
            widget.x = x;
            widget.y = y;
            widget.render(matrixStack, mouseX, mouseY, partialTicks);
            x += widget.getWidth();
        }
    }

    @Override
    protected int getMinimumWidth() {
        return widgets.stream().map(Widget::getWidth).reduce(Integer::sum).orElse(0);
    }

    public static class Builder {
        private final List<Option> options = new LinkedList<>();

        public Builder add(ITextComponent name, Runnable onPress, Supplier<StickyWidget.State> stateSupplier) {
            options.add(new Option(name, onPress, stateSupplier));
            return this;
        }

        public ExclusiveButtonsGui build() {
            List<Widget> widgets = options.stream()
                    .map(option -> new StickyWidget(0, 0, 0, 0, option.name, option.onPress, option.stateSupplier))
                    .collect(Collectors.toList());
            return new ExclusiveButtonsGui(widgets);
        }

        private static class Option {
            private ITextComponent name;
            private Runnable onPress;
            private Supplier<StickyWidget.State> stateSupplier;

            public Option(ITextComponent name, Runnable onPress, Supplier<StickyWidget.State> stateSupplier) {
                this.name = name;
                this.onPress = onPress;
                this.stateSupplier = stateSupplier;
            }
        }
    }

}
