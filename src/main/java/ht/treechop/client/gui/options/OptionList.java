package ht.treechop.client.gui.options;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.AbstractOptionList;

public class OptionList extends AbstractOptionList<OptionRow> {

    private static final int ROW_WIDTH = 400;

    public OptionList(Minecraft minecraft, int width, int height, int top, int bottom, int itemHeight) {
        super(minecraft, width, height, top, bottom, itemHeight);

        // Don't render background
        func_244605_b(false);
    }

    public void addRow(LabeledOptionRow row) {
        this.addEntry(row);
    }

    public int getRowWidth() {
        return ROW_WIDTH;
    }

    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32;
    }

}
