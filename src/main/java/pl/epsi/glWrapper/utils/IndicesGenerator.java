package pl.epsi.glWrapper.utils;

import org.lwjgl.opengl.GL11;

public class IndicesGenerator {

    /**
     * Generates an indices array for the given vertex count and GL primitive type.
     * @param vertexCount Number of vertices.
     * @param glPrimitive OpenGL primitive type (e.g., GL11.GL_TRIANGLES).
     * @return int[] indices array.
     *         Returns null if generation isn't supported for the primitive.
     */
    public static int[] generateIndices(int vertexCount, int glPrimitive) {
        if (vertexCount <= 0) {
            return new int[0];
        }

        switch (glPrimitive) {
            case GL11.GL_LINES:
                if (vertexCount % 2 != 0) {
                    vertexCount -= 1;
                }
                return generateSequentialIndices(vertexCount);

            case GL11.GL_LINE_STRIP:
                return generateLineStripIndices(vertexCount);

            case GL11.GL_TRIANGLES:
                if (vertexCount % 3 != 0) {
                    vertexCount -= vertexCount % 3;
                }
                return generateSequentialIndices(vertexCount);

            case GL11.GL_TRIANGLE_STRIP:
                return generateTriangleStripIndices(vertexCount);

            case GL11.GL_TRIANGLE_FAN:
                return generateTriangleFanIndices(vertexCount);

            default:
                return null;
        }
    }

    private static int[] generateSequentialIndices(int count) {
        int[] indices = new int[count];
        for (int i = 0; i < count; i++) {
            indices[i] = i;
        }
        return indices;
    }

    private static int[] generateLineStripIndices(int vertexCount) {
        if (vertexCount < 2) return new int[0];
        int[] indices = new int[(vertexCount - 1) * 2];
        int idx = 0;
        for (int i = 0; i < vertexCount - 1; i++) {
            indices[idx++] = i;
            indices[idx++] = i + 1;
        }
        return indices;
    }

    private static int[] generateTriangleStripIndices(int vertexCount) {
        if (vertexCount < 3) return new int[0];
        int triangleCount = vertexCount - 2;
        int[] indices = new int[triangleCount * 3];
        int idx = 0;
        for (int i = 0; i < triangleCount; i++) {
            if (i % 2 == 0) {
                indices[idx++] = i;
                indices[idx++] = i + 1;
                indices[idx++] = i + 2;
            } else {
                indices[idx++] = i + 1;
                indices[idx++] = i;
                indices[idx++] = i + 2;
            }
        }
        return indices;
    }

    private static int[] generateTriangleFanIndices(int vertexCount) {
        if (vertexCount < 3) return new int[0];
        int triangleCount = vertexCount - 2;
        int[] indices = new int[triangleCount * 3];
        int idx = 0;
        for (int i = 1; i <= triangleCount; i++) {
            indices[idx++] = 0;       // center vertex
            indices[idx++] = i;
            indices[idx++] = i + 1;
        }
        return indices;
    }

}
