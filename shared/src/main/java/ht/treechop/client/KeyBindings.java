package ht.treechop.client;

import com.mojang.blaze3d.platform.InputConstants;
import ht.treechop.TreeChop;
import ht.treechop.client.gui.screen.ClientSettingsScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class KeyBindings {
    public static final String CATEGORY = "HT's TreeChop";

    public static final List<ActionableKeyBinding> allKeyBindings = new LinkedList<>();

    public static void registerKeyMappings(Consumer<KeyMapping> register) {
        registerKeyBinding("toggle_chopping", InputConstants.UNKNOWN, Client::toggleChopping, register);
        registerKeyBinding("cycle_sneak_behavior", InputConstants.UNKNOWN, Client::cycleSneakBehavior, register);
        registerKeyBinding("open_settings_overlay", getKey(GLFW.GLFW_KEY_N), Client::toggleSettingsOverlay, register);
    }

    private static ActionableKeyBinding registerKeyBinding(String name, InputConstants.Key defaultKey, Runnable callback, Consumer<KeyMapping> register) {
        ActionableKeyBinding keyBinding = new ActionableKeyBinding(
                String.format("%s.key.%s", TreeChop.MOD_ID, name),
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

    protected static class ActionableKeyBinding extends KeyMapping {

        private final Runnable callback;

        public ActionableKeyBinding(String resourceName, InputConstants.Key inputByCode, Runnable callback) {
            super(resourceName, InputConstants.Type.KEYSYM, inputByCode.getValue(), CATEGORY);
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
