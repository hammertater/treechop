package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.util.Sprite;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToggleWidget extends Widget {

    private final Supplier<State> stateSupplier;
    private final Runnable onPress;

    public ToggleWidget(int x, int y, Runnable onPress, Supplier<State> stateSupplier) {
        super(x, y, Sprite.TOGGLE_BUTTON_OFF.width, Sprite.TOGGLE_BUTTON_OFF.height);
        this.onPress = onPress;
        this.stateSupplier = stateSupplier;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        this.active = !stateSupplier.get().isLocked;
        renderButton(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onClick(int mouseX, int mouseY, int button) {
        onPress.run();
    }

    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        Sprite.setRenderState(1f);

        final EnumMap<State, Sprite> spriteForState = new EnumMap<State, Sprite>(Stream.of(
                Pair.of(State.OFF, Sprite.TOGGLE_BUTTON_OFF),
                Pair.of(State.ON, Sprite.TOGGLE_BUTTON_ON),
                Pair.of(State.LOCKED_OFF, Sprite.LOCKED_TOGGLE_BUTTON_OFF),
                Pair.of(State.LOCKED_ON, Sprite.LOCKED_TOGGLE_BUTTON_ON)
        ).collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));

        final EnumMap<State, Sprite> spriteForHoveredState = new EnumMap<State, Sprite>(Stream.of(
                Pair.of(State.OFF, Sprite.HIGHLIGHTED_TOGGLE_BUTTON_OFF),
                Pair.of(State.ON, Sprite.HIGHLIGHTED_TOGGLE_BUTTON_ON),
                Pair.of(State.LOCKED_OFF, Sprite.LOCKED_TOGGLE_BUTTON_OFF),
                Pair.of(State.LOCKED_ON, Sprite.LOCKED_TOGGLE_BUTTON_ON)
        ).collect(Collectors.toMap(Pair::getLeft, Pair::getRight)));

        State state = stateSupplier.get();
        Sprite sprite = isHovered() ? spriteForHoveredState.get(state) : spriteForState.get(state);
        sprite.blit(getBox().getLeft(), getBox().getTop());
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
