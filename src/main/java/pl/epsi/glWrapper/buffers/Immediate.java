package pl.epsi.glWrapper.buffers;

import org.lwjgl.opengl.GL33;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.utils.Identifier;

public class Immediate extends BufferBuilder {

    protected Immediate(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat, int VAO) {
        super(id, drawMode, vertexFormat, VAO);
    }

    protected Immediate(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(new Identifier("DefaultImmediate#" + drawMode + "#" + vertexFormat), drawMode, vertexFormat, GL33.glGenVertexArrays());
    }

    public Immediate(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(id, drawMode, vertexFormat, GL33.glGenVertexArrays());
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
