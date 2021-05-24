package ht.treechop.client.gui.element;

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

    public void setColumnWidths(int biggestLeftColumnWidth, int biggestRightColumnWidth) {
    }

}
