package pl.epsi.glWrapper.callbacks;

import org.lwjgl.glfw.GLFW;
import pl.epsi.glWrapper.functional.DoubleBinaryConsumer;
import pl.epsi.glWrapper.functional.IntBinaryConsumer;
import pl.epsi.glWrapper.functional.IntTernaryConsumer;

import java.util.ArrayList;

public class Mouse {

    private static float lastX, lastY;

    private static final ArrayList<DoubleBinaryConsumer> mouseMoveCallbacks = new ArrayList<>();

    private static final ArrayList<IntTernaryConsumer> mouseClickCallbacks = new ArrayList<>();
    private static final ArrayList<IntBinaryConsumer> mouseButtonDownCallbacks = new ArrayList<>();
    private static final ArrayList<IntBinaryConsumer> mouseButtonUpCallbacks = new ArrayList<>();

    private static final ArrayList<DoubleBinaryConsumer> mouseScrollCallbacks = new ArrayList<>();

    private static boolean firstMouse = true;

    public static void onMouseMove(long window, double posX, double posY) {
        if (firstMouse) {
            lastX = (float) posX;
            lastY = (float) posY;
            firstMouse = false;
            return;
        }

        float dx = (float) posX - lastX;
        float dy = (float) (lastY - posY);
        lastX = (float) posX;
        lastY = (float) posY;

        mouseMoveCallbacks.forEach(t -> t.applyAsDouble(dx, dy));
    }

    public static void onMouseButton(long window, int button, int action, int mods) {
        mouseClickCallbacks.forEach(t -> t.applyAsInt(button, action, mods));

        if (action == GLFW.GLFW_PRESS) mouseButtonDownCallbacks.forEach(t -> t.applyAsInt(button, mods));
        if (action == GLFW.GLFW_RELEASE) mouseButtonUpCallbacks.forEach(t -> t.applyAsInt(button, mods));
    }

    public static void onMouseScroll(long window, double xOffset, double yOffset) {
        mouseScrollCallbacks.forEach(t -> t.applyAsDouble(xOffset, yOffset));
    }

    // region Registering Callbacks
    public static void registerMouseMoveCallback(DoubleBinaryConsumer c) { Mouse.mouseMoveCallbacks.add(c); }

    public static void registerMouseClickCallback(IntTernaryConsumer c) { Mouse.mouseClickCallbacks.add(c); }
    public static void registerMouseButtonDownCallback(IntBinaryConsumer c) { Mouse.mouseButtonDownCallbacks.add(c); }
    public static void registerMouseButtonUpCallback(IntBinaryConsumer c) { Mouse.mouseButtonUpCallbacks.add(c); }

    public static void registerMouseScrollCallback(DoubleBinaryConsumer c) { Mouse.mouseScrollCallbacks.add(c); }
    // endregion

}
