package ht.treechop.client.gui.screen;

import ht.treechop.client.gui.IGuiEventListener;
import net.minecraft.client.gui.GuiScreen;

public abstract class Screen extends GuiScreen implements IGuiEventListener {

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        final int LEFT_MOUSE_BUTTON = 0;
        if (button == LEFT_MOUSE_BUTTON) {
            onClick(mouseX, mouseY, button);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        // Do nothing
    }

}
