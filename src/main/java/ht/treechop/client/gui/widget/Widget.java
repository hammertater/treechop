package ht.treechop.client.gui.widget;

import ht.treechop.client.gui.element.NestedGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.init.SoundEvents;

public abstract class Widget extends NestedGui {

    public boolean active;
    public boolean hovered;

    public Widget(int x, int y, int width, int height) {
        setBox(x, y, width, height);
        this.active = true;
        this.hovered = false;
    }

    public int getWidth() {
        return getBox().getWidth();
    }

    public int getHeight() {
        return getBox().getHeight();
    }

    @Override
    public int getMinimumWidth() {
        return getWidth();
    }

    @Override
    public int getMinimumHeight() {
        return getHeight();
    }

    protected boolean isHovered() {
        return hovered;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        hovered = isMouseOver(mouseX, mouseY);
    }

    public void onClick(int mouseX, int mouseY, int button) {
        playPressSound(Minecraft.getMinecraft().getSoundHandler());
    }

    private void playPressSound(SoundHandler soundHandlerIn) {
        soundHandlerIn.playSound(getPressSound());
    }

    protected PositionedSoundRecord getPressSound() {
        return PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F);
    }

    protected int getYImage(boolean hovered) {
        int i = 1;
        if (!this.active) {
            i = 0;
        } else if (hovered) {
            i = 2;
        }

        return i;
    }
}
