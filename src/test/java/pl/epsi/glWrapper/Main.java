package pl.epsi.glWrapper;

import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.system.MemoryStack;
import pl.epsi.glWrapper.buffers.BufferBuilder;
import pl.epsi.glWrapper.buffers.Buffers;
import pl.epsi.glWrapper.buffers.DrawMode;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.shader.ShaderProgram;
import pl.epsi.glWrapper.shader.UniformProvider;
import pl.epsi.glWrapper.utils.*;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL43C.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;

    @Test
    public void runNormal() {
        run();
    }

    @Test
    public void runRenderdoc() throws InterruptedException {
        Thread.sleep(5000);
        run();
    }

    public void run() {
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 600, "Test Env", NULL, NULL);
        Renderer.updateProjMatrix(800, 600);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
            Renderer.updateProjMatrix(width, height);
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(0);

        // Make the window visible
        glfwShowWindow(window);

        loop();
    }

    private void loop() {
        GL.createCapabilities();

        //if (glGetInteger(GL_MAJOR_VERSION) >= 4 && glGetInteger(GL_MINOR_VERSION) >= 3) {
        //    glEnable(GL_DEBUG_OUTPUT);
        //    glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        //    glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
        //        String msg = GLDebugMessageCallback.getMessage(length, message);
        //        System.err.println("GL DEBUG: " + msg);
        //    }, 0);
        //}

        // Set the clear color
        glClearColor(0.0f, 0.2f, 0.3f, 0.0f);
        Identifier texture = new Identifier("textures/testImage.jpg");

        BufferBuilder customBuilder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
        //customBuilder.withFragmentShader(new Identifier("shaders/core/test_shader.fsh"));
        customBuilder.withShader(new ShaderProgram(new Identifier("shaders/core/test_shader.vsh"), new Identifier("shaders/core/test_shader.fsh")));

        BufferBuilder.AttributeType type = BufferBuilder.AttributeType.register("TEST_TYPE");
        BufferBuilder.AttributeContainer customContainer = new BufferBuilder.AttributeContainer(type, 1, GlNumberType.FLOAT, 3);
        customBuilder.withVertexAttribute(customContainer);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            //DrawContext.drawRect(customBuilder, 0, 0, 255, 255, 0, true);
            customBuilder.withUniform(new UniformProvider() {
                @Override
                public void apply(ShaderProgram program) {
                    program.uniformFloat("time", (float) glfwGetTime());
                }
            });

            customBuilder.vertex(255, 255, 0)  .color(1, 1, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 255, 0)    .color(1, 0, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 0, 0)      .color(1, 1, 0, 0).attrib(type, 1f);
            customBuilder.vertex(0, 0, 0)      .color(1, 1, 0, 0).attrib(type, 1f);
            customBuilder.vertex(255, 0, 0)    .color(1, 0, 0, 1).attrib(type, 0f);
            customBuilder.vertex(255, 255, 0)  .color(1, 1, 1, 0).attrib(type, 0f);
            customBuilder.addToQueue();

            DrawContext.drawTexture(Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR_TEXTURE),
                    texture, 300, 300, 600, 600, 0, 0, 0, 16, 16, true);

            DrawContext.drawGradient(000, 600, 150, 450, 0, GradientDirection.TOP_TO_BOTTOM, 0xFF002991, 0xFF00916f, true);
            DrawContext.drawGradient(000, 450, 150, 300, 0, GradientDirection.BOTTOM_TO_TOP, 0xFF002991, 0xFF00916f, true);
            DrawContext.drawGradient(150, 600, 300, 450, 0, GradientDirection.LEFT_TO_RIGHT, 0xFF002991, 0xFF00916f, true);
            DrawContext.drawGradient(150, 450, 300, 300, 0, GradientDirection.RIGHT_TO_LEFT, 0xFF002991, 0xFF00916f, true);

            Renderer.render();

            glfwSwapBuffers(window); // swap the color buffers

            glfwPollEvents();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

}
