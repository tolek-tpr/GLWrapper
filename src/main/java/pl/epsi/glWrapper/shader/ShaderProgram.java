package pl.epsi.glWrapper.shader;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import pl.epsi.glWrapper.utils.Identifier;
import pl.epsi.glWrapper.utils.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

public class ShaderProgram {

    public int ID;

    public ShaderProgram(Identifier vertex, Identifier fragment, Identifier json) {
        this(vertex.asInputStream(), fragment.asInputStream(), json.asInputStream());
    }

    public ShaderProgram(Identifier vertex, Identifier fragment) {
        this(vertex.asInputStream(), fragment.asInputStream());
    }

    private ShaderProgram(InputStream vertexStream, InputStream fragmentStream) {
        this(vertexStream, fragmentStream, null);
    }

    private ShaderProgram(InputStream vertexStream, InputStream fragmentStream, InputStream jsonStream) {
        try {
            String vCode = Utils.readFile(vertexStream);
            String fCode = Utils.readFile(fragmentStream);

            int vertex, fragment;

            vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(vertex, vCode);
            GL20.glCompileShader(vertex);
            checkErrors(vertex, "SHADER-VERTEX");

            fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(fragment, fCode);
            GL20.glCompileShader(fragment);
            checkErrors(fragment, "SHADER-FRAGMENT");

            ID = GL20.glCreateProgram();
            GL20.glAttachShader(ID, vertex);
            GL20.glAttachShader(ID, fragment);
            GL20.glLinkProgram(ID);
            checkErrors(ID, "PROGRAM");

            GL20.glDeleteShader(vertex);
            GL20.glDeleteShader(fragment);
        } catch (IOException e) {
            System.err.println("An error occurred when building the shader");
            e.printStackTrace();
        }
    }

    public void uniformMat4f(String name, Matrix4f mat) {
        int location = GL20.glGetUniformLocation(this.ID, name);
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        mat.get(buf);
        GL20.glUniformMatrix4fv(location, false, buf);
    }

    public void uniformTexture(String name, int slot) {
        int location = GL20.glGetUniformLocation(this.ID, name);
        GL20.glUniform1i(location, slot);
    }

    public void uniformIntArray(String name, int[] array) {
        int location = GL20.glGetUniformLocation(this.ID, name);
        GL20.glUniform1iv(location, array);
    }

    public void use() {
        GL20.glUseProgram(ID);
    }

    private void checkErrors(int shader, String type) {
        if (type.equals("PROGRAM")) {
            if (!GL20.glGetProgramInfoLog(shader).isEmpty()) {
                System.out.println(GL20.glGetProgramInfoLog(shader));
                throw new RuntimeException("Program failed!");
            }
        } else {
            if (!GL20.glGetShaderInfoLog(shader).isEmpty()) {
                System.out.println(GL20.glGetShaderInfoLog(shader));
                throw new RuntimeException("Shader " + type + " didn't load properly!");
            }
        }
    }

}
