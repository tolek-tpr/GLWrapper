package pl.epsi.glWrapper.buffers;

import pl.epsi.glWrapper.utils.Utils;

import java.util.HashMap;

import static pl.epsi.glWrapper.buffers.BufferBuilder.BufferInfo;

public class Buffers {

    private static final HashMap<BufferInfo, BufferBuilder> bufferBuilders = new HashMap<>();
    private static final HashMap<BufferInfo, Immediate> immediateBuffers = new HashMap<>();

    public static BufferBuilder getBuffer(DrawMode drawMode, DrawMode.VertexFormat format) {
        return getBuffer(new BufferInfo(drawMode, format));
    }

    public static BufferBuilder getBuffer(BufferInfo bufferInfo) {
        if (!bufferBuilders.containsKey(bufferInfo)) genBuffer(BufferType.BUILDER, bufferInfo);
        return Utils.getOrThrow(bufferBuilders, bufferInfo,
                new IllegalArgumentException("Unable to find BufferBuilder for DrawMode " + bufferInfo.drawMode() + " and VertexFormat " + bufferInfo.vertexFormat()));
    }

    public static Immediate getImmediate(DrawMode drawMode, DrawMode.VertexFormat format) {
        return getImmediate(new BufferInfo(drawMode, format));
    }

    public static Immediate getImmediate(BufferInfo bufferInfo) {
        if (!immediateBuffers.containsKey(bufferInfo)) genBuffer(BufferType.IMMEDIATE, bufferInfo);
        return Utils.getOrThrow(immediateBuffers, bufferInfo,
                new IllegalArgumentException("Unable to find Immediate buffer for DrawMode " + bufferInfo.drawMode() + " and VertexFormat " + bufferInfo.vertexFormat()));
    }

    private static void genBuffer(BufferType type, BufferInfo info) {
        if (type == BufferType.BUILDER) {
            bufferBuilders.put(info, new BufferBuilder(info));
        } else {
            immediateBuffers.put(info, new Immediate(info));
        }
    }

    enum BufferType {

        IMMEDIATE,
        BUILDER

    }

}
