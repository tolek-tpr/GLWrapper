package pl.epsi.glWrapper.buffers;

import java.util.ArrayList;

public class Buffers {

    private static final ArrayList<BufferBuilder> bufferBuilders = new ArrayList<>();
    private static final ArrayList<Immediate> immediateBuffers = new ArrayList<>();
    private static final ArrayList<BufferBuilder3D> bufferBuilders3D = new ArrayList<>();
    private static final ArrayList<Immediate3D> immediateBuffers3D = new ArrayList<>();

    public static void init() {
        for (DrawMode drawMode : DrawMode.values()) {
            for (DrawMode.VertexFormat vertexFormat : DrawMode.VertexFormat.values()) {
                addBufferBuilderAndImmediate(new BufferBuilder(drawMode, vertexFormat));
            }
        }
    }

    public static void init3D() {
        for (DrawMode drawMode : DrawMode.values()) {
            for (DrawMode.VertexFormat vertexFormat : DrawMode.VertexFormat.values()) {
                addBufferBuilder3DAndImmediate3D(new BufferBuilder3D(drawMode, vertexFormat));
            }
        }
    }

    public static BufferBuilder getBuffer(DrawMode drawMode, DrawMode.VertexFormat format) {
        if (bufferBuilders.isEmpty()) init();
        for (BufferBuilder buffer : bufferBuilders) {
            if (buffer.drawMode == drawMode && buffer.vertexFormat == format) return buffer;
        }

        throw new IllegalArgumentException("Unable to find BufferBuilder for DrawMode " + drawMode + " and VertexFormat " + format);
    }

    public static Immediate getImmediate(DrawMode drawMode, DrawMode.VertexFormat format) {
        if (immediateBuffers.isEmpty()) init();
        for (Immediate immediate : immediateBuffers) {
            if (immediate.drawMode == drawMode && immediate.vertexFormat == format) return immediate;
        }

        throw new IllegalArgumentException("Unable to find Immediate buffer for DrawMode " + drawMode + " and VertexFormat " + format);
    }

    public static BufferBuilder3D getBuffer3D(DrawMode drawMode, DrawMode.VertexFormat format) {
        if (bufferBuilders3D.isEmpty()) init3D();
        for (BufferBuilder3D buffer : bufferBuilders3D) {
            if (buffer.drawMode == drawMode && buffer.vertexFormat == format) return buffer;
        }

        throw new IllegalArgumentException("Unable to find BufferBuilder3D for DrawMode " + drawMode + " and VertexFormat " + format);
    }

    public static Immediate3D getImmediate3D(DrawMode drawMode, DrawMode.VertexFormat format) {
        if (immediateBuffers3D.isEmpty()) init3D();
        for (Immediate3D immediate : immediateBuffers3D) {
            if (immediate.drawMode == drawMode && immediate.vertexFormat == format) return immediate;
        }

        throw new IllegalArgumentException("Unable to find Immediate3D for DrawMode " + drawMode + " and VertexFormat " + format);
    }

    private static void addBufferBuilderAndImmediate(BufferBuilder buffer) {
        bufferBuilders.add(buffer);
        immediateBuffers.add(Immediate.fromBuffer(buffer));
    }

    private static void addBufferBuilder3DAndImmediate3D(BufferBuilder3D buffer) {
        bufferBuilders3D.add(buffer);
        immediateBuffers3D.add(Immediate3D.fromBuffer(buffer));
    }

}
