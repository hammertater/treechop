package ht.treechop.client.gui.util;

public interface IPositionalGui {

    ScreenBox getBox();

    void setBox(ScreenBox box);

    default void setBox(int left, int top, int width, int height) {
        setBox(new ScreenBox(left, top, width, height));
    }

}
