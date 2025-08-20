package pl.epsi.glWrapper.buffers;

import org.lwjgl.opengl.GL30;

import java.util.HashMap;

import static pl.epsi.glWrapper.buffers.BufferBuilder.AttributeType;

public class VertexBufferHandler {

    private static final HashMap<AttributeType, Integer> vertexBufferObjects = new HashMap<>();

    public static Integer getVBO(AttributeType type) {
        if (!vertexBufferObjects.containsKey(type)) vertexBufferObjects.put(type, GL30.glGenBuffers());
        return vertexBufferObjects.get(type);
    }

    public static HashMap<AttributeType, Integer> getVBOsForVertexFormat(DrawMode.VertexFormat format) {
        HashMap<AttributeType, Integer> VBOs = new HashMap<>();

        format.getAttributes().forEach((attribute) -> {
            VBOs.put(attribute.getType(), getVBO(attribute.getType()));
        });

        return VBOs;
    }

}
