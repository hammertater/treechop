package ht.treechop.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import ht.treechop.client.gui.util.Sprite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToggleWidget extends Widget {

    private final Supplier<State> stateSupplier;
    private final Runnable onPress;

    public ToggleWidget(int x, int y, Runnable onPress, Supplier<State> stateSupplier) {
        super(x, y, Sprite.TOGGLE_BUTTON_OFF.width, Sprite.TOGGLE_BUTTON_OFF.height, new StringTextComponent(""));
        this.onPress = onPress;
        this.stateSupplier = stateSupplier;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.active = !stateSupplier.get().isLocked;
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void onClick(double mouseX, double mouseY) {
        onPress.run();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Sprite.setRenderState(this.alpha);

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
        sprite.blit(matrixStack, x, y);
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
