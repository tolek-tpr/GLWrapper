package pl.epsi.glWrapper.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;
import pl.epsi.glWrapper.buffers.BufferBuilder;
import pl.epsi.glWrapper.shader.ShaderProgram;
import pl.epsi.glWrapper.shader.ShaderProgramKeys;
import pl.epsi.glWrapper.utils.GlNumberType;
import pl.epsi.glWrapper.utils.IndicesGenerator;
import pl.epsi.glWrapper.utils.Lists;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Renderer {

    private static final ArrayList<BufferBuilder> renderQueue = new ArrayList<>();

    public static Matrix4f projMatrix;

    public static void render() {
        renderQueue.forEach(Renderer::renderBuffer);
        renderQueue.clear();
    }

    public static void renderBuffer(BufferBuilder bufferBuilder) {
        bufferBuilder.setupVao();

        BufferBuilder.AttributeContainer positionContainer = bufferBuilder.getContainerForType(BufferBuilder.AttributeType.get("POSITION"));
        int vertexCount = positionContainer.getCount() / positionContainer.getSize();
        int gl_primitive = bufferBuilder.bufferInfo.drawMode().getGlMode();

        int[] indices = IndicesGenerator.generateIndices(vertexCount, gl_primitive);
        if (indices == null) throw new IllegalStateException("Unsupported primitive type " + gl_primitive + " (" + bufferBuilder.bufferInfo.drawMode() + ")");

        GL30.glBindVertexArray(bufferBuilder.getVAO());

        ByteBuffer mappedEBO = bufferBuilder.getEBO().getBuffer();
        mappedEBO.position(0);
        mappedEBO.asIntBuffer().put(indices);

        ShaderProgram shader = bufferBuilder.getShader() == null ? ShaderProgramKeys.getByVertexFormat(bufferBuilder.bufferInfo.vertexFormat()) : bufferBuilder.getShader();

        shader.use();
        bufferBuilder.uniformProviders.forEach(up -> up.apply(shader));

        GL30.glDrawElements(bufferBuilder.bufferInfo.drawMode().getGlMode(), indices.length, GL30.GL_UNSIGNED_INT, 0);

        GL30.glBindVertexArray(0);

        bufferBuilder.getVBO().rotate();
        bufferBuilder.clear();
    }

    public static void updateProjMatrix(int width, int height) {
        projMatrix = new Matrix4f().ortho(0.0f, width, 0.0f, height, -1, 1);
    }

    public static void addToRenderQueue(BufferBuilder builder) {
        if (!Renderer.renderQueue.contains(builder))
            Renderer.renderQueue.add(builder);
    }

}
