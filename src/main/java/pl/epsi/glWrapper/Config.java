package pl.epsi.glWrapper;

import org.lwjgl.glfw.GLFW;
import pl.epsi.glWrapper.callbacks.Keyboard;
import pl.epsi.glWrapper.callbacks.Mouse;
import pl.epsi.glWrapper.callbacks.Viewport;
import pl.epsi.glWrapper.camera.Camera;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.utils.RenderSystem;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class Config {

    public static void setupCallbacks(long window) {
        GLFW.glfwSetCursorPosCallback(window, Mouse::onMouseMove);
        GLFW.glfwSetMouseButtonCallback(window, Mouse::onMouseButton);
        GLFW.glfwSetScrollCallback(window, Mouse::onMouseScroll);

        GLFW.glfwSetKeyCallback(window, Keyboard::onKey);

        GLFW.glfwSetFramebufferSizeCallback(window, Viewport::onViewportSizeChange);
    }

    public static void setupRenderer(int windowWidth, int windowHeight, boolean updateViewport) {
        Renderer.updateProjMatrix(windowWidth, windowHeight);
        Viewport.registerViewportSizeChangeCallback((w, h) -> {
            Renderer.updateProjMatrix(w, h);
            if (updateViewport) RenderSystem.setViewport(0, 0, w, h);
        });
    }

    public static void setupCameraCallbacks(Camera camera) {
        Mouse.registerMouseScrollCallback(camera::handleScroll);
    }

    public static void setupCloseDetection(long window, int key) {
        Keyboard.registerKeyboardKeyUpCallback((keyCode, scanCode, mods) -> glfwSetWindowShouldClose(window, keyCode == key));
    }

}
