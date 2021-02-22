package ht.treechop.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;

public class ConfigScreen extends ModSettingsScreen {

    public ConfigScreen(Minecraft minecraft, Screen screen) {
        super();
    }

    @Override
    public void init(Minecraft minecraft, int width, int height) {
        super.init(minecraft, width, height);
        optionsRowList.setBackgroundEnabled(true);
    }

//    @Override
//    public void renderBackground(MatrixStack matrixStack) {
//        super.renderBackground(matrixStack);
//        this.renderDirtBackground(0);
//    }

}
