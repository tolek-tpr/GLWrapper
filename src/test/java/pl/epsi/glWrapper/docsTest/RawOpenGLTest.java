package pl.epsi.glWrapper.docsTest;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import pl.epsi.glWrapper.utils.Identifier;
import pl.epsi.glWrapper.utils.Utils;

import java.io.IOException;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class RawOpenGLTest {

    long window;

    public void run() throws IOException, InterruptedException {
        Thread.sleep(5000);

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

    private void loop() throws IOException {
        GL.createCapabilities();

        glClearColor(0.0f, 0.2f, 0.3f, 0.0f);

        int VAO, VBO, EBO;
        VBO = glGenBuffers();
        EBO = glGenBuffers();
        VAO = glGenVertexArrays();

        glBindVertexArray(VAO);

        float[] vertices = {
                -0.5f, 0.5f, 0.0f,     1, 0, 0, 1,
                -0.5f, -0.5f, 0.0f,    0, 1, 0, 1,
                0.5f, -0.5f, 0.0f,     0, 0, 1, 1,
                0.5f, 0.5f, 0.0f,      1, 1, 0, 1
        };

        int[] indices = {
                0, 1, 2,
                2, 3, 0
        };

        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        int VERTEX_SIZE_BYTES = 7 * Float.BYTES;
        int POS_SIZE_BYTES = 3 * Float.BYTES;

        glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_SIZE_BYTES);

        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        // Shader setup
        // This will not draw anything since the proj matrix uniform isnt provided in this example so do not use this + the vertices are in NDC instead of screen space
        String vCode = Utils.readFile(new Identifier("shaders/core/position_color_program.vsh").asInputStream());
        String fCode = Utils.readFile(new Identifier("shaders/core/position_color_program.fsh").asInputStream());

        int vertex, fragment, shaderID;

        vertex = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertex, vCode);
        glCompileShader(vertex);

        fragment = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragment, fCode);
        glCompileShader(fragment);

        shaderID = glCreateProgram();
        glAttachShader(shaderID, vertex);
        glAttachShader(shaderID, fragment);
        glLinkProgram(shaderID);

        glDeleteShader(vertex);
        glDeleteShader(fragment);

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glBindBuffer(GL_ARRAY_BUFFER, VBO);
            glBindVertexArray(VAO);
            glUseProgram(shaderID);

            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glBindVertexArray(0);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
            glUseProgram(0);

            glfwSwapBuffers(window);

            glfwPollEvents();
        }

        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

}
