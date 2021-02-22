package ht.treechop.client.gui.options;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.list.AbstractOptionList;

public abstract class OptionRow extends AbstractOptionList.Entry<OptionRow> {

    public abstract void resize(int width);

    // This is just here to fix parameter names
    public abstract void render(MatrixStack matrixStack, int entryIdx, int top, int left, int width, int height, int mouseX, int mouseY, boolean someBoolean, float partialTicks);

    protected abstract int getMinimumWidth();

}
