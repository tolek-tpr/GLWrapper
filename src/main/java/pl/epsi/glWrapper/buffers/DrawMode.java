package pl.epsi.glWrapper.buffers;

import org.lwjgl.opengl.GL11;
import pl.epsi.glWrapper.shader.ProjectionMatrixUniformProvider;
import pl.epsi.glWrapper.shader.TextureSamplerUniformProvider;
import pl.epsi.glWrapper.shader.UniformProvider;
import pl.epsi.glWrapper.utils.GlNumberType;
import pl.epsi.glWrapper.utils.Lists;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static pl.epsi.glWrapper.buffers.BufferBuilder.AttributeContainer;
import static pl.epsi.glWrapper.buffers.BufferBuilder.AttributeType;

public enum DrawMode {

    LINES(GL11.GL_LINES),
    LINE_STRIP(GL11.GL_LINE_STRIP),
    TRIANGLES(GL11.GL_TRIANGLES),
    TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP),
    TRIANGLE_FAN(GL11.GL_TRIANGLE_FAN);

    private final int mode;

    DrawMode(int mode) {
        this.mode = mode;
    }

    public static DrawMode getMode(int mode) {
        for (var drawMode : values()) {
            if (drawMode.mode == mode) {
                return drawMode;
            }
        }
        throw new IllegalArgumentException("Unknown DrawMode: " + mode);
    }

    public int getGlMode() {
        return this.mode;
    }

    public enum VertexFormat {

        // VertexFormat: pos(float float float)
        POSITION(0, Lists.asArrayList(
                new AttributeContainer(AttributeType.get("POSITION"), 3, GlNumberType.FLOAT, 0)
        ), Lists.asArrayList(
                ProjectionMatrixUniformProvider.class
        )),
        // VertexFormat: pos(float float float), color(float float float float)
        POSITION_COLOR(1, Lists.asArrayList(
                new AttributeContainer(AttributeType.get("POSITION"), 3, GlNumberType.FLOAT, 0),
                new AttributeContainer(AttributeType.get("COLOR"), 4, GlNumberType.FLOAT, 1)
        ), Lists.asArrayList(
                ProjectionMatrixUniformProvider.class
        )),
        // VertexFormat: pos(float float float), UV(float float float(textureIDX))
        POSITION_TEXTURE(2, Lists.asArrayList(
                new AttributeContainer(AttributeType.get("POSITION"), 3, GlNumberType.FLOAT, 0),
                new AttributeContainer(AttributeType.get("TEXTURE"), 3, GlNumberType.FLOAT, 2)
        ), Lists.asArrayList(
                ProjectionMatrixUniformProvider.class, TextureSamplerUniformProvider.class
        )),
        // VertexFormat: pos(float float float), color(float float float float), UV(float float float(textureIDX))
        POSITION_COLOR_TEXTURE(3, Lists.asArrayList(
                new AttributeContainer(AttributeType.get("POSITION"), 3, GlNumberType.FLOAT, 0),
                new AttributeContainer(AttributeType.get("COLOR"), 4, GlNumberType.FLOAT, 1),
                new AttributeContainer(AttributeType.get("TEXTURE"), 3, GlNumberType.FLOAT, 2)
        ), Lists.asArrayList(
                ProjectionMatrixUniformProvider.class, TextureSamplerUniformProvider.class
        ));

        private final int format;
        private final ArrayList<AttributeContainer> attributes;
        private final ArrayList<Class<? extends UniformProvider>> uniformProviders;

        VertexFormat(int format, ArrayList<AttributeContainer> attributes, ArrayList<Class<? extends UniformProvider>> uniformProviders) {
            this.format = format;
            this.attributes = attributes;
            this.uniformProviders = uniformProviders;
        }

        public static VertexFormat getVertexFormat(int format) {
            for (var VertexFormat : values()) {
                if (VertexFormat.format == format) {
                    return VertexFormat;
                }
            }
            throw new IllegalArgumentException("Unknown VertexFormat: " + format);
        }

        public ArrayList<AttributeContainer> getAttributes() {
            return this.attributes.stream().map(AttributeContainer::copy).collect(Collectors.toCollection(ArrayList::new));
        }
        public int getAttributesSize() {
            return this.attributes.size();
        }
        public ArrayList<Class<? extends UniformProvider>> getUniformProviders() { return this.uniformProviders; }

    }

}
