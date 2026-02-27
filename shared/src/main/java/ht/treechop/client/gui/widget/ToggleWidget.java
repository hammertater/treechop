package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.util.Sprite;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.EnumMap;
import java.util.function.Supplier;

public class ToggleWidget extends AbstractWidget {

    private static final EnumMap<State, Sprite> spriteForState = new EnumMap<>(State.class);
    static {
        spriteForState.put(State.OFF, Sprite.TOGGLE_BUTTON_OFF);
        spriteForState.put(State.ON, Sprite.TOGGLE_BUTTON_ON);
        spriteForState.put(State.LOCKED_OFF, Sprite.LOCKED_TOGGLE_BUTTON_OFF);
        spriteForState.put(State.LOCKED_ON, Sprite.LOCKED_TOGGLE_BUTTON_ON);
    }

    private static final EnumMap<State, Sprite> spriteForHoveredState = new EnumMap<>(State.class);
    static {
        spriteForHoveredState.put(State.OFF, Sprite.HIGHLIGHTED_TOGGLE_BUTTON_OFF);
        spriteForHoveredState.put(State.ON, Sprite.HIGHLIGHTED_TOGGLE_BUTTON_ON);
        spriteForHoveredState.put(State.LOCKED_OFF, Sprite.LOCKED_TOGGLE_BUTTON_OFF);
        spriteForHoveredState.put(State.LOCKED_ON, Sprite.LOCKED_TOGGLE_BUTTON_ON);
    }

    private final Supplier<State> stateSupplier;
    private final Runnable onPress;

    public ToggleWidget(int x, int y, Runnable onPress, Supplier<State> stateSupplier) {
        super(x, y, Sprite.TOGGLE_BUTTON_OFF.width, Sprite.TOGGLE_BUTTON_OFF.height, Component.empty());
        this.onPress = onPress;
        this.stateSupplier = stateSupplier;
    }

    public void onClick(double mouseX, double mouseY) {
        onPress.run();
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTicks) {
        this.active = !stateSupplier.get().isLocked;

        State state = stateSupplier.get();
        Sprite sprite = isHoveredOrFocused() ? spriteForHoveredState.get(state) : spriteForState.get(state);
        sprite.blit(RenderType::guiTextured, gui, getX(), getY(), this.alpha);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO
    }

    public enum State {
        ON(true, false),
        OFF(false, false),
        LOCKED_ON(true, true),
        LOCKED_OFF(false, true)
        ;

        public final boolean isOn;
        private final boolean isLocked;

        State(boolean isOn, boolean isLocked) {
            this.isOn = isOn;
            this.isLocked = isLocked;
        }

        public static State of(boolean enabled, boolean canBeEnabled) {
            if (canBeEnabled) {
                return enabled ? ON : OFF;
            } else {
                return enabled ? LOCKED_ON : LOCKED_OFF;
            }
        }
    }

}
