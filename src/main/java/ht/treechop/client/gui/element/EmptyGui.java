package ht.treechop.client.gui.element;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.Collections;
import java.util.List;

public class EmptyGui extends NestedGui {

    private int width;
    private int height;

    public EmptyGui(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getMinimumWidth() {
        return width;
    }

    public int getMinimumHeight() {
        return height;
    }

    public int getLeftColumnWidth() {
        return 0;
    }

    public int getRightColumnWidth() {
        return 0;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public List<? extends IGuiEventListener> children() {
        return Collections.emptyList();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    }

    public void setColumnWidths(int biggestLeftColumnWidth, int biggestRightColumnWidth) {
    }
}
