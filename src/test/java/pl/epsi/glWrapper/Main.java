package pl.epsi.glWrapper;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import pl.epsi.glWrapper.buffers.BufferBuilder;
import pl.epsi.glWrapper.buffers.BufferBuilder3D;
import pl.epsi.glWrapper.buffers.Buffers;
import pl.epsi.glWrapper.buffers.DrawMode;
import pl.epsi.glWrapper.callbacks.Viewport;
import pl.epsi.glWrapper.camera.Camera;
import pl.epsi.glWrapper.callbacks.Keyboard;
import pl.epsi.glWrapper.callbacks.Mouse;
import pl.epsi.glWrapper.model.Mesh;
import pl.epsi.glWrapper.model.ModelLoader;
import pl.epsi.glWrapper.render.RenderPass;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.shader.ShaderProgram;
import pl.epsi.glWrapper.utils.*;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;

    public static final Camera CAMERA = new Camera(new Vector3f(400, 300, 600), new Vector3f(0, 1, 0), 270, 0);

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
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        Config.setupCallbacks(window);
        Config.setupRenderer(800, 600, true, true);
        Config.setupCameraCallbacks(CAMERA);
        Config.setupCloseDetection(window, GLFW_KEY_ESCAPE);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        Keyboard.registerKeyboardKeyUpCallback((keyCode, scanCode, mods) -> {
            if (keyCode == GLFW_KEY_T) {
                RenderSystem.set3D(!RenderSystem.is3D());
            }
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

        // Set the clear color
        glClearColor(0.0f, 0.2f, 0.3f, 0.0f);
        Identifier texture = new Identifier("textures/testImage.jpg");

        RenderPass pass2D = Renderer.getRenderPass2D();
        BufferBuilder customBuilder = pass2D.withBuilder(new BufferBuilder(new Identifier("customBuilder"),
                DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR));
        //customBuilder.withFragmentShader(new Identifier("shaders/core/test_shader.fsh"));
        customBuilder.withShader(new ShaderProgram(new Identifier("shaders/core/test_shader.vsh"), new Identifier("shaders/core/test_shader.fsh")));

        BufferBuilder.AttributeType type = BufferBuilder.AttributeType.register("TEST_TYPE");
        BufferBuilder.AttributeContainer customContainer = new BufferBuilder.AttributeContainer(type, 1, GlNumberType.FLOAT, 3);
        customBuilder.withVertexAttribute(customContainer);

        Mesh m = ModelLoader.loadSingleMesh(new Identifier("models/cube.obj"));
        m.modelMatrix.scale(16, 16, 16);

        while (!glfwWindowShouldClose(window)) {
            //Profiler.startFrame();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            CAMERA.tick(window);

            RenderPass pass3D = Renderer.begin3D();


            //Profiler.startSection("For loop (Drawing meshes)");
            BufferBuilder3D builder3D = pass3D.getBuffer3D(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_TEXTURE_NORMAL);
            for (int i = 0; i < 32; i++) {
                for (int y = 0; y < 32; y++) {
                    Mesh temp = m.copy();
                    temp.modelMatrix.translate(i * 3, y * 3, 0);

                    builder3D.mesh(temp);

                }
            }
            //Profiler.endSection();
            builder3D.addToQueue();
            //Profiler.startSection("Render");

            Renderer.render();

            //Profiler.endSection();

            //Profiler.endFrame();

            System.out.println("FPS: " + 1.0 / Time.getDeltaTime());

            glfwSwapBuffers(window); // swap the color buffers

            glfwPollEvents();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

}
