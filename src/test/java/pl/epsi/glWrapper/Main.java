package pl.epsi.glWrapper;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import pl.epsi.glWrapper.buffers.BufferBuilder;
import pl.epsi.glWrapper.buffers.Buffers;
import pl.epsi.glWrapper.buffers.DrawMode;
import pl.epsi.glWrapper.camera.Camera;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.shader.ShaderProgram;
import pl.epsi.glWrapper.shader.UniformProvider;
import pl.epsi.glWrapper.utils.*;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;

    public static final Camera CAMERA = new Camera(new Vector3f(400, 300, 600), new Vector3f(0, 0, 0));

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
        Renderer.updateProjMatrix(800, 600, true);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            if (key == GLFW_KEY_T && action == GLFW_RELEASE)
                System.out.println(CAMERA.getPosition());
            CAMERA.onKey(window, key, scancode, mods);
        });

        glfwSetCursorPosCallback(window, CAMERA::onMouse);
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
            Renderer.updateProjMatrix(width, height, true);
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
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        loop();
    }

    private void loop() {
        GL.createCapabilities();

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
            Renderer.viewMatrix = CAMERA.getViewMatrix();

            //DrawContext.drawRect(customBuilder, 0, 0, 255, 255, 0, true);
            customBuilder.withUniform(program -> program.uniformFloat("time", (float) glfwGetTime()));

            customBuilder.vertex(255, 255, 0)  .color(1, 1, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 255, 0)    .color(1, 0, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 0, 0)      .color(1, 1, 0, 0).attrib(type, 1f);
            customBuilder.vertex(0, 0, 0)      .color(1, 1, 0, 0).attrib(type, 1f);
            customBuilder.vertex(255, 0, 0)    .color(1, 0, 0, 1).attrib(type, 0f);
            customBuilder.vertex(255, 255, 0)  .color(1, 1, 1, 0).attrib(type, 0f);
            customBuilder.addToQueue();

            DrawContext.drawTexture(texture, 300, 300, 600, 600, 0, 0, 0, 16, 16, true);

            //DrawContext.drawGradient(000, 600, 150, 450, 0, GradientDirection.TOP_TO_BOTTOM, 0xFF002991, 0xFF00916f, true);
            //DrawContext.drawGradient(000, 450, 150, 300, 0, GradientDirection.BOTTOM_TO_TOP, 0xFF002991, 0xFF00916f, true);
            //DrawContext.drawGradient(150, 600, 300, 450, 0, GradientDirection.LEFT_TO_RIGHT, 0xFF002991, 0xFF00916f, true);
            //DrawContext.drawGradient(150, 450, 300, 300, 0, GradientDirection.RIGHT_TO_LEFT, 0xFF002991, 0xFF00916f, true);

            BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
            float[] sc = Utils.getArgbColors0To1(0xFF002991);
            float[] ec = Utils.getArgbColors0To1(0xFF00916f);
            GL30.glEnable(GL_DEPTH_TEST);

            builder.vertex(150, 450, 0).color(sc[0], sc[1], sc[2], sc[3]);
            builder.vertex(000, 450, 0).color(sc[0], sc[1], sc[2], sc[3]);
            builder.vertex(000, 600, 0).color(ec[0], ec[1], ec[2], ec[3]);
            builder.vertex(000, 600, 0).color(ec[0], ec[1], ec[2], ec[3]);
            builder.vertex(150, 600, 0).color(ec[0], ec[1], ec[2], ec[3]);
            builder.vertex(150, 450, 0).color(sc[0], sc[1], sc[2], sc[3]);

            builder.vertex(150, 450, 150).color(sc[0], sc[1], sc[2], sc[3]);
            builder.vertex(150, 450, 000).color(sc[0], sc[1], sc[2], sc[3]);
            builder.vertex(150, 600, 000).color(ec[0], ec[1], ec[2], ec[3]);
            builder.vertex(150, 600, 000).color(ec[0], ec[1], ec[2], ec[3]);
            builder.vertex(150, 600, 150).color(ec[0], ec[1], ec[2], ec[3]);
            builder.vertex(150, 450, 150).color(sc[0], sc[1], sc[2], sc[3]);
            builder.addToQueue();

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
