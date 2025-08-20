package pl.epsi.glWrapper.buffers;

import pl.epsi.glWrapper.render.Renderer;

public class Immediate extends BufferBuilder {

    public Immediate(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        super(drawMode, vertexFormat);
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
