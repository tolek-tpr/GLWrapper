package pl.epsi.glWrapper.docsTest;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import pl.epsi.glWrapper.buffers.BufferBuilder;
import pl.epsi.glWrapper.buffers.Buffers;
import pl.epsi.glWrapper.buffers.DrawMode;
import pl.epsi.glWrapper.render.Renderer;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class GLWrapperTest {

    long window;

    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        window = glfwCreateWindow(800, 600, "Test Env", NULL, NULL);

        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);

        glfwSwapInterval(1);

        glfwShowWindow(window);

        loop();
    }

    private void loop() {
        GL.createCapabilities();

        glClearColor(0.0f, 0.2f, 0.3f, 0.0f);

        Renderer.updateProjMatrix(800, 600, true);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLE_STRIP, DrawMode.VertexFormat.POSITION_COLOR);

            builder.vertex(200, 400, 0).color(1, 0, 1, 0);
            builder.vertex(600, 400, 0).color(1, 1, 0, 0);
            builder.vertex(200, 200, 0).color(1, 0, 0, 1);
            builder.vertex(600, 200, 0).color(1, 1, 1, 0);
            builder.addToQueue();

            Renderer.render();

            glfwSwapBuffers(window);

            glfwPollEvents();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

}
