package ht.treechop.client.gui.util;

public class ScreenBox {

    protected int left;
    protected int top;
    protected int width;
    protected int height;

    public ScreenBox(int left, int top, int width, int height) {
        set(left, top, width, height);
    }

    public void set(int left, int top, int width, int height) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    public void set(ScreenBox other) {
        set(other.getLeft(), other.getTop(), other.getWidth(), other.getHeight());
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight()  {
        return height;
    }

    public int getCenterX() {
        return getLeft() + getWidth() / 2;
    }

    public int getCenterY() {
        return getTop() + getHeight() / 2;
    }

}
