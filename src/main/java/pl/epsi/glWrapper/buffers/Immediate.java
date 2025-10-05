package pl.epsi.glWrapper.buffers;

import org.lwjgl.opengl.GL33;
import pl.epsi.glWrapper.buffers.gpu.GpuBuffer;
import pl.epsi.glWrapper.buffers.gpu.MappableGpuBuffer;
import pl.epsi.glWrapper.buffers.gpu.MappableGpuRingBuffer;
import pl.epsi.glWrapper.render.Renderer;

public class Immediate extends BufferBuilder {

    public Immediate(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        super(drawMode, vertexFormat);
    }

    public Immediate(BufferInfo info) {
        super(info);
    }

    public static Immediate fromBuffer(BufferBuilder buffer) {
        return new Immediate(buffer.bufferInfo);
    }

    public void end() {
        this.addToQueue();
    }

    @Override
    public void addToQueue() {
        Renderer.renderBuffer(this);
    }

}
