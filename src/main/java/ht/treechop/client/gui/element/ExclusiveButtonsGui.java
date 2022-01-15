package ht.treechop.client.gui.element;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.widget.StickyWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExclusiveButtonsGui extends NestedGui {

    private final List<AbstractWidget> widgets;
    private final Supplier<Component> tooltipSupplier;

    protected ExclusiveButtonsGui(Collection<AbstractWidget> widgets, Supplier<Component> tooltipSupplier) {
        super(0, 0, 0, 0, TextComponent.EMPTY);
        this.widgets = Lists.newArrayList(widgets);
        this.tooltipSupplier = tooltipSupplier;
    }

    public void expand(int width) {
        if (getMinimumWidth() < width) {
            int targetWidth = width / widgets.size();
            List<AbstractWidget> smallerWidgets = widgets.stream().filter(widget -> widget.getWidth() <= targetWidth).collect(Collectors.toList());
            List<AbstractWidget> biggerWidgets = widgets.stream().filter(widget -> widget.getWidth() > targetWidth).collect(Collectors.toList());
            int totalWidthForSmallers = width - biggerWidgets.stream().map(AbstractWidget::getWidth).reduce(Integer::sum).orElse(0);
            int i = 0;
            // Do this incrementally to account for rounding errors
            for (AbstractWidget widget : smallerWidgets) {
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
    public List<? extends GuiEventListener> children() {
        return widgets;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        int x = getBox().getLeft();
        int y = getBox().getTop();

        int maxX = x;
        int maxY = y;

        for (AbstractWidget widget : widgets) {
            widget.x = maxX;
            widget.y = y;
            widget.render(poseStack, mouseX, mouseY, partialTicks);

            maxX = Math.max(maxX, maxX + widget.getWidth());
            maxY = Math.max(maxY, y + widget.getHeight());
        }

        isHovered = mouseX >= x && mouseY >= y && mouseX < maxX && mouseY < maxY;
        if (isHovered()) {
            GUIUtil.showTooltip(mouseX, mouseY, tooltipSupplier.get());
        }
    }

    @Override
    public int getMinimumWidth() {
        return widgets.stream().map(AbstractWidget::getWidth).reduce(Integer::sum).orElse(0);
    }

    @Override
    public int getMinimumHeight() {
        return widgets.stream().map(AbstractWidget::getHeight).reduce(Integer::max).orElse(0);
    }

    @Override
    public void updateNarration(NarrationElementOutput out) {
        // TODO
    }

    public static class Builder {
        private final List<Option> options = new LinkedList<>();

        public Builder add(Component name, Runnable onPress, Supplier<StickyWidget.State> stateSupplier) {
            options.add(new Option(name, onPress, stateSupplier));
            return this;
        }

        public ExclusiveButtonsGui build(Supplier<Component> tooltipSupplier) {
            List<AbstractWidget> widgets = options.stream()
                    .map(option -> new StickyWidget(0, 0, 0, 0, option.name, option.onPress, option.stateSupplier))
                    .collect(Collectors.toList());
            return new ExclusiveButtonsGui(widgets, tooltipSupplier);
        }

        private static class Option {
            private Component name;
            private Runnable onPress;
            private Supplier<StickyWidget.State> stateSupplier;

            public Option(Component name, Runnable onPress, Supplier<StickyWidget.State> stateSupplier) {
                this.name = name;
                this.onPress = onPress;
                this.stateSupplier = stateSupplier;
            }
        }
    }

}
