package ht.treechop.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import ht.treechop.client.gui.util.GUIUtil;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

import java.util.function.Supplier;

public class StickyWidget extends Widget {

    private final Supplier<Boolean> stateSupplier;
    private final Runnable onPress;

    public StickyWidget(int x, int y, int width, int height, ITextComponent name, Runnable onPress, Supplier<Boolean> stateSupplier) {
        super(x, y, width, height, name);
        this.onPress = onPress;
        this.stateSupplier = stateSupplier;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.active = !stateSupplier.get();
        this.height = Math.min(this.height, GUIUtil.BUTTON_HEIGHT);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void onClick(double mouseX, double mouseY) {
        onPress.run();
    }

}
