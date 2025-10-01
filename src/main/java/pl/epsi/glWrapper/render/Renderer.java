package pl.epsi.glWrapper.render;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;
import pl.epsi.glWrapper.buffers.BufferBuilder;
import pl.epsi.glWrapper.buffers.BufferBuilder3D;
import pl.epsi.glWrapper.shader.ShaderProgram;
import pl.epsi.glWrapper.shader.ShaderProgramKeys;
import pl.epsi.glWrapper.utils.*;

import java.util.ArrayList;

public class Renderer {

    private static final ArrayList<BufferBuilder> renderQueue = new ArrayList<>();

    public static Matrix4f projMatrix = new Matrix4f().identity();
    public static Matrix4f viewMatrix = new Matrix4f().identity();

    private static final RenderPass RENDER_PASS_3D = new RenderPass(true);
    private static final RenderPass RENDER_PASS_2D = new RenderPass(false);

    private static boolean a = false;
    private static int[] indices;

    public static void render() {
        if (!RenderSystem.is3D()) viewMatrix = new Matrix4f().identity();
        renderQueue.forEach(Renderer::renderBuffer);
        //renderQueue.clear();
        Time.onEndFrame();
    }

    public static void renderBuffer(BufferBuilder bufferBuilder) {
        //Profiler.startSection("VAO setup");
        bufferBuilder.setupAttribPointers();
        bufferBuilder.setupVao();
        //Profiler.endSection();

        //Profiler.startSection("Render 1");
        BufferBuilder.AttributeContainer positionContainer = bufferBuilder.getContainerForType(BufferBuilder.AttributeType.get("POSITION"));
        int vertexCount = positionContainer.getCount() / positionContainer.getSize();
        int gl_primitive = bufferBuilder.drawMode.getGlMode();

        if (!a) {
            if (bufferBuilder.getIndices().isEmpty()) {
                indices = IndicesGenerator.generateIndices(vertexCount, gl_primitive);

                if (indices == null)
                    throw new IllegalStateException("Unsupported primitive type " + gl_primitive + " (" + bufferBuilder.drawMode + ")");
            } else {
                indices = new int[bufferBuilder.getIndices().size()];
                for (int i = 0; i < bufferBuilder.getIndices().size(); i++) {
                    indices[i] = bufferBuilder.getIndices().get(i);
                }
            }
        }

        GL30.glBindVertexArray(bufferBuilder.getVAO());

        if (!a) {
            GL30.glBufferData(GL30.GL_ELEMENT_ARRAY_BUFFER, indices, GL30.GL_DYNAMIC_DRAW);
            a = true;
        }

        ShaderProgram shader = bufferBuilder.getShader() == null ?
                (bufferBuilder instanceof BufferBuilder3D ? ShaderProgramKeys.getByVertexFormat3D(bufferBuilder.vertexFormat) :
                        ShaderProgramKeys.getByVertexFormat(bufferBuilder.vertexFormat)) :
                bufferBuilder.getShader();
        GL30.glUseProgram(shader.ID);

        bufferBuilder.uniformProviders.forEach(up -> up.apply(shader));

        GL30.glDrawElements(bufferBuilder.drawMode.getGlMode(), indices.length, GL30.GL_UNSIGNED_INT, 0);

        GL30.glBindVertexArray(0);
        bufferBuilder.getVBO().unbind();
        bufferBuilder.getEBO().unbind();
        //Profiler.endSection();
    }

    public static void updateProjMatrix(int width, int height, boolean perspective) {
        projMatrix.identity();
        if (perspective) {
            projMatrix.perspective((float) Math.toRadians(70.0f),
                    (float) width / height,
                    0.01f, 10000f);
        } else {
            projMatrix.ortho(0, width, 0, height, -1, 1);
        }
    }

    public static void addToRenderQueue(BufferBuilder builder) {
        if (!Renderer.renderQueue.contains(builder))
            Renderer.renderQueue.add(builder);
    }

    public static RenderPass begin3D() {
        // Do stuff
        return RENDER_PASS_3D;
    }

    public static RenderPass begin2D() {
        return RENDER_PASS_2D;
    }

    public static RenderPass getRenderPass3D() { return Renderer.RENDER_PASS_3D; }
    public static RenderPass getRenderPass2D() { return Renderer.RENDER_PASS_2D; }

}
