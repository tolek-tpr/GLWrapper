package pl.epsi.glWrapper.buffers;

import org.lwjgl.opengl.GL33;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.utils.Identifier;

public class Immediate3D extends BufferBuilder3D {

    protected Immediate3D(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat, int VAO) {
        super(id, drawMode, vertexFormat, VAO);
    }

    protected Immediate3D(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(new Identifier("DefaultBuilder3D#" + drawMode + "#" + vertexFormat), drawMode, vertexFormat, GL33.glGenVertexArrays());
    }

    public Immediate3D(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(id, drawMode, vertexFormat, GL33.glGenVertexArrays());
    }

    public static Immediate3D fromBuffer(BufferBuilder3D buffer) {
        return new Immediate3D(buffer.drawMode, buffer.vertexFormat);
    }

    public void end() {
        this.addToQueue();
    }

    @Override
    public void addToQueue() {
        Renderer.renderBuffer(this);
    }

}
