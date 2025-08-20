package pl.epsi.glWrapper.buffers;

import java.util.ArrayList;

public class Buffers {

    private static final ArrayList<BufferBuilder> bufferBuilders = new ArrayList<>();
    private static final ArrayList<Immediate> immediateBuffers = new ArrayList<>();

    public static void init() {
        for (DrawMode drawMode : DrawMode.values()) {
            for (DrawMode.VertexFormat vertexFormat : DrawMode.VertexFormat.values()) {
                addBufferBuilderAndImmediate(new BufferBuilder(drawMode, vertexFormat));
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

    private static void addBufferBuilderAndImmediate(BufferBuilder buffer) {
        bufferBuilders.add(buffer);
        immediateBuffers.add(Immediate.fromBuffer(buffer));
    }

}
