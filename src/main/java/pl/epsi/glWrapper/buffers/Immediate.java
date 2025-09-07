package pl.epsi.glWrapper.buffers;

import org.lwjgl.opengl.GL33;
import pl.epsi.glWrapper.buffers.gpu.GpuBuffer;
import pl.epsi.glWrapper.buffers.gpu.MappedGpuBuffer;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.utils.Identifier;

public class Immediate extends BufferBuilder {

    protected Immediate(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat, int VAO, MappedGpuBuffer VBO, GpuBuffer EBO) {
        super(id, drawMode, vertexFormat, VAO, VBO, EBO);
    }

    protected Immediate(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(new Identifier("DefaultImmediate#" + drawMode + "#" + vertexFormat), drawMode, vertexFormat, GL33.glGenVertexArrays(),
                new MappedGpuBuffer(GpuBuffer.BufferTarget.ARRAY_BUFFER, GpuBuffer.BufferUsage.DYNAMIC_DRAW, 3, 1000 * 1024),
                new GpuBuffer(GpuBuffer.BufferTarget.ELEMENT_ARRAY_BUFFER, GpuBuffer.BufferUsage.DYNAMIC_DRAW));
    }

    public Immediate(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(id, drawMode, vertexFormat, GL33.glGenVertexArrays(),
                new MappedGpuBuffer(GpuBuffer.BufferTarget.ARRAY_BUFFER, GpuBuffer.BufferUsage.DYNAMIC_DRAW, 3, 1000 * 1024),
                new GpuBuffer(GpuBuffer.BufferTarget.ELEMENT_ARRAY_BUFFER, GpuBuffer.BufferUsage.DYNAMIC_DRAW));
    }

    public static Immediate fromBuffer(BufferBuilder buffer) {
        return new Immediate(buffer.drawMode, buffer.vertexFormat);
    }

    public void end() {
        this.addToQueue();
    }

    @Override
    public void addToQueue() {
        Renderer.renderBuffer(this);
    }

}
