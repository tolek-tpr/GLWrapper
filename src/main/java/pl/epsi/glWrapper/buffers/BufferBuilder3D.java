package pl.epsi.glWrapper.buffers;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;
import pl.epsi.glWrapper.buffers.gpu.GpuBuffer;
import pl.epsi.glWrapper.buffers.gpu.MappedGpuBuffer;
import pl.epsi.glWrapper.model.Material;
import pl.epsi.glWrapper.model.Mesh;
import pl.epsi.glWrapper.utils.GlNumberType;
import pl.epsi.glWrapper.utils.Identifier;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;

public class BufferBuilder3D extends BufferBuilder {

    private final ArrayList<Mesh> meshes = new ArrayList<>();

    private boolean a = false;

    protected BufferBuilder3D(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat, int VAO, MappedGpuBuffer VBO, GpuBuffer EBO) {
        super(id, drawMode, vertexFormat, VAO, VBO, EBO);
        this.withVertexAttribute(new AttributeContainer(AttributeType.get("MODEL_INDEX"), 1, GlNumberType.INT, 4));
    }

    protected BufferBuilder3D(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(new Identifier("DefaultBuilder3D#" + drawMode + "#" + vertexFormat), drawMode, vertexFormat, GL30.glGenVertexArrays(),
                new MappedGpuBuffer(GpuBuffer.BufferTarget.ARRAY_BUFFER, GpuBuffer.BufferUsage.DYNAMIC_DRAW, 3, 1000 * 1024),
                new GpuBuffer(GpuBuffer.BufferTarget.ELEMENT_ARRAY_BUFFER, GpuBuffer.BufferUsage.DYNAMIC_DRAW));
    }

    public BufferBuilder3D(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(id, drawMode, vertexFormat, GL30.glGenVertexArrays(),
                new MappedGpuBuffer(GpuBuffer.BufferTarget.ARRAY_BUFFER, GpuBuffer.BufferUsage.DYNAMIC_DRAW, 3, 1000 * 1024),
                new GpuBuffer(GpuBuffer.BufferTarget.ELEMENT_ARRAY_BUFFER, GpuBuffer.BufferUsage.DYNAMIC_DRAW));
    }

    public BufferBuilder3D mesh(Mesh mesh) {
        // TODO: Add check for mesh size and if it is too big give it its own vbo

        meshes.add(mesh);
        ArrayList<Mesh> temp = new ArrayList<>(meshes);

        for (Vector3f v : mesh.getVertices()) {
            this.vertex(v.x, v.y, v.z);
            this.index(temp.size() - 1);
        }

        mesh.getUvs().forEach(v -> this.texture(v.x, v.y, mesh.getMaterial().getDiffuseMap()));
        mesh.getNormals().forEach(v -> this.normal(v.x, v.y, v.z));

        meshes.clear();
        meshes.addAll(temp);

        int highestIndex = this.getIndices().isEmpty() ? 0 : Collections.max(this.getIndices()) + 1;
        mesh.getIndices().forEach(idx -> this.getIndices().add(idx + highestIndex));

        return this;
    }

    @Override
    public int getVAO() {
        int vao = super.getVAO();

        if (!a) {
            int modelMatrixSSBO = GL45.glGenBuffers();

            FloatBuffer matricesBuffer = MemoryUtil.memAllocFloat(16 * this.meshes.size());

            this.meshes.forEach(m -> {
                float[] tempBuffer = new float[16];
                m.modelMatrix.get(tempBuffer);
                matricesBuffer.put(tempBuffer);
            });

            matricesBuffer.flip();

            GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, modelMatrixSSBO);
            GL45.glBufferData(GL45.GL_SHADER_STORAGE_BUFFER, matricesBuffer, GL45.GL_STATIC_DRAW);
            GL45.glBindBufferBase(GL45.GL_SHADER_STORAGE_BUFFER, 0, modelMatrixSSBO);

            MemoryUtil.memFree(matricesBuffer);

            int materialSSBO = GL45.glGenBuffers();

            FloatBuffer materialBuffer = MemoryUtil.memAllocFloat(12 * this.meshes.size());

            this.meshes.forEach(m -> {
                Material mat = m.getMaterial();
                materialBuffer.put(mat.getColor().x).put(mat.getColor().y).put(mat.getColor().z).put(mat.getColor().w);
                materialBuffer.put(mat.getEmissiveColor().x).put(mat.getEmissiveColor().y).put(mat.getEmissiveColor().z).put(mat.getEmissiveColor().w);
                materialBuffer.put(mat.getMetallic()).put(mat.getRoughness());
                materialBuffer.position(materialBuffer.position() + 2);

            });

            materialBuffer.flip();

            GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, materialSSBO);
            GL45.glBufferData(GL45.GL_SHADER_STORAGE_BUFFER, materialBuffer, GL45.GL_STATIC_DRAW);
            GL45.glBindBufferBase(GL45.GL_SHADER_STORAGE_BUFFER, 1, materialSSBO);

            MemoryUtil.memFree(materialBuffer);

            GL45.glBindBuffer(GL45.GL_SHADER_STORAGE_BUFFER, 0);
            a = true;
        }

        return vao;
    }

    @Override
    public void clear() {
        super.clear();
        this.meshes.clear();
    }

}
