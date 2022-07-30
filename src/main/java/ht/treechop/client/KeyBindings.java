package ht.treechop.client;

import com.mojang.blaze3d.platform.InputConstants;
import ht.treechop.TreeChopMod;
import ht.treechop.client.gui.screen.ClientSettingsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class KeyBindings {

    public static final String CATEGORY = "HT's TreeChop";

    public static final List<ActionableKeyBinding> allKeyBindings = new LinkedList<>();

    public static void registerKeyMappings(Consumer<KeyMapping> register) {
        registerKeyBinding("toggle_chopping", getKey(GLFW.GLFW_KEY_UNKNOWN), Client::toggleChopping, register);
        registerKeyBinding("toggle_felling", getKey(GLFW.GLFW_KEY_UNKNOWN), Client::toggleFelling, register);
        registerKeyBinding("cycle_sneak_behavior", getKey(GLFW.GLFW_KEY_UNKNOWN), Client::cycleSneakBehavior, register);
        registerKeyBinding("open_settings_overlay", getKey(GLFW.GLFW_KEY_N), Client::toggleSettingsOverlay, register);
    }

    private static ActionableKeyBinding registerKeyBinding(String name, InputConstants.Key defaultKey, Runnable callback, Consumer<KeyMapping> register) {
        ActionableKeyBinding keyBinding = new ActionableKeyBinding(
                String.format("%s.key.%s", TreeChopMod.MOD_ID, name),
                defaultKey,
                callback
        );
        register.accept(keyBinding);

        allKeyBindings.add(keyBinding);

        return keyBinding;
    }

    static InputConstants.Key getKey(int key) {
        return InputConstants.getKey(key, 0);
    }

    public static void buttonPressed(int keyCode, int keyState) {
        for (ActionableKeyBinding keyBinding : allKeyBindings) {
            if (keyCode == keyBinding.getKey().getValue() && keyState == GLFW.GLFW_PRESS) {
                keyBinding.onPress();
                return;
            }
        }
    }

    protected static class ActionableKeyBinding extends KeyMapping {

        private final Runnable callback;

        public ActionableKeyBinding(String resourceName, InputConstants.Key inputByCode, Runnable callback) {
            super(resourceName, KeyConflictContext.GUI, inputByCode, CATEGORY);
            this.callback = () -> {
                Screen screen = Minecraft.getInstance().screen;
                if (screen == null || screen instanceof ClientSettingsScreen) {
                    callback.run();
                }
            };
        }

        public void onPress() {
            callback.run();
        }

    }
}
