package ht.treechop.client;

import ht.treechop.TreeChopMod;
import ht.treechop.capabilities.ChopSettingsCapability;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

public class KeyBindings {

    public static final String CATEGORY = "HT's TreeChop";
    public static ActionableKeyBinding toggleChopping;

    public static List<ActionableKeyBinding> allKeyBindings = new LinkedList<>();

    public static void clientSetup(FMLClientSetupEvent event) {
        registerKeyBinding("toggle_chopping", getKey(GLFW.GLFW_KEY_N), Client::toggleChopping);
        registerKeyBinding("toggle_felling", getKey(GLFW.GLFW_KEY_UNKNOWN), Client::toggleFelling);
        registerKeyBinding("cycle_sneak_behavior", getKey(GLFW.GLFW_KEY_UNKNOWN), Client::cycleSneakBehavior);
    }

    private static ActionableKeyBinding registerKeyBinding(String name, InputMappings.Input defaultKey, Runnable callback) {
        ActionableKeyBinding keyBinding = new ActionableKeyBinding(
                String.format("key.%s.%s", TreeChopMod.MOD_ID, name),
                defaultKey,
                callback
        );

        ClientRegistry.registerKeyBinding(keyBinding);

        allKeyBindings.add(keyBinding);

        return keyBinding;
    }

    static InputMappings.Input getKey(int key) {
        return InputMappings.Type.KEYSYM.getOrMakeInput(key);
    }

    public static void buttonPressed(InputEvent.KeyInputEvent event) {
        if (event.isCanceled() || Minecraft.getInstance().currentScreen != null) {
            return;
        }

        for (ActionableKeyBinding keyBinding : allKeyBindings) {
            if (event.getKey() == keyBinding.getKey().getKeyCode() && event.getAction() == GLFW.GLFW_PRESS) {
                keyBinding.onPress();
                return;
            }
        }
    }

    private static class ActionableKeyBinding extends KeyBinding {

        private final Runnable callback;

        public ActionableKeyBinding(String resourceName, InputMappings.Input inputByCode, Runnable callback) {
            super(resourceName, KeyConflictContext.GUI, inputByCode, CATEGORY);
            this.callback = callback;
        }

        public void onPress() {
            callback.run();
        }

    }
}
