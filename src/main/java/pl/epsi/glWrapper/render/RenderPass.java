package pl.epsi.glWrapper.render;

import pl.epsi.glWrapper.buffers.BufferBuilder;
import pl.epsi.glWrapper.buffers.BufferBuilder3D;
import pl.epsi.glWrapper.buffers.Buffers;
import pl.epsi.glWrapper.buffers.DrawMode;
import pl.epsi.glWrapper.utils.Identifier;
import pl.epsi.glWrapper.utils.Lists;

import java.util.ArrayList;

public class RenderPass {

    private final boolean is3D;

    private final ArrayList<BufferBuilder> buffers = new ArrayList<>();

    public RenderPass(boolean is3D) {
        this.is3D = is3D;
    }

    public BufferBuilder3D getBuffer3D(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        if (is3D) {
            return Buffers.getBuffer3D(drawMode, vertexFormat);
        }
        throw new IllegalArgumentException("Requested 3D Buffer for 2D Render Pass!");
    }

    public BufferBuilder3D getBuffer3D(Identifier id) {
        if (is3D) {
            for (BufferBuilder buffer : buffers) {
                if (buffer.getID() == id && buffer instanceof BufferBuilder3D) return (BufferBuilder3D) buffer;
            }
            throw new IllegalArgumentException("Requested buffer with id: " + id + " but could not find one! Make sure to register custom buffers!");
        }
        throw new IllegalArgumentException("Requested 3D Buffer for 2D Render Pass!");
    }

    /**
     *  This function is used for getting the default buffers, if you would like to get your custom registered
     *  buffer, use the overload of this method and give your custom buffer's identifier.
     */
    public BufferBuilder getBuffer(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        if (!is3D) {
            return Buffers.getBuffer(drawMode, vertexFormat);
        }
        throw new IllegalArgumentException("Requested 2D Buffer for 3D Render Pass!");
    }

    public BufferBuilder getBuffer(Identifier id) {
        if (!is3D) {
            for (BufferBuilder buffer : buffers) {
                if (buffer.getID() == id) return buffer;
            }
            throw new IllegalArgumentException("Requested buffer with id: " + id + " but could not find one! Make sure to register custom buffers!");
        }
        throw new IllegalArgumentException("Requested 2D Buffer for 3D Render Pass!");
    }

    public BufferBuilder withBuilder(BufferBuilder bufferBuilder) {
        Lists.add(buffers, bufferBuilder);
        return bufferBuilder;
    }

    private BufferBuilder findBuffer(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        for (BufferBuilder buffer : buffers) {
            if (buffer.drawMode == drawMode && buffer.vertexFormat == vertexFormat) return buffer;
        }
        return null;
    }

}
