package pl.epsi.glWrapper.callbacks;

import org.lwjgl.glfw.GLFW;
import pl.epsi.glWrapper.functional.IntQuadConsumer;
import pl.epsi.glWrapper.functional.IntTernaryConsumer;

import java.util.ArrayList;

public class Keyboard {

    public static final ArrayList<IntQuadConsumer> keyboardKeyCallbacks = new ArrayList<>();
    public static final ArrayList<IntTernaryConsumer> keyboardKeyNoActionCallbacks = new ArrayList<>();
    public static final ArrayList<IntTernaryConsumer> keyboardKeyDownCallbacks = new ArrayList<>();
    public static final ArrayList<IntTernaryConsumer> keyboardKeyUpCallbacks = new ArrayList<>();

    public static void onKey(long window, int keyCode, int scanCode, int action, int mods) {
        keyboardKeyCallbacks.forEach(t -> t.applyAsInt(keyCode, scanCode, action, mods));
        keyboardKeyNoActionCallbacks.forEach(t -> t.applyAsInt(keyCode, scanCode, mods));

        if (action == GLFW.GLFW_PRESS) keyboardKeyDownCallbacks.forEach(t -> t.applyAsInt(keyCode, scanCode, mods));
        if (action == GLFW.GLFW_RELEASE) keyboardKeyUpCallbacks.forEach(t -> t.applyAsInt(keyCode, scanCode, mods));
    }

    public static boolean isKeyPressed(long window, int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    // region Registering Callbacks
    public static void registerKeyboardKeyCallback(IntQuadConsumer c) { Keyboard.keyboardKeyCallbacks.add(c); }
    public static void registerKeyboardKeyNoActionCallback(IntTernaryConsumer c) { Keyboard.keyboardKeyNoActionCallbacks.add(c); }
    public static void registerKeyboardKeyDownCallback(IntTernaryConsumer c) { Keyboard.keyboardKeyDownCallbacks.add(c); }
    public static void registerKeyboardKeyUpCallback(IntTernaryConsumer c) { Keyboard.keyboardKeyUpCallbacks.add(c); }
    // endregion

}
