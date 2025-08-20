package pl.epsi.glWrapper.utils;

import pl.epsi.glWrapper.buffers.BufferBuilder;
import pl.epsi.glWrapper.buffers.Buffers;
import pl.epsi.glWrapper.buffers.DrawMode;

public class DrawContext {

    // region Rectangle
    public static void drawRect(int x1, int y1, int x2, int y2, int z, boolean queue) {
        DrawContext.drawRect(x1, y1, x2, y2, z, 0xFFFFFFFF, queue);
    }

    public static void drawRect(int x1, int y1, int x2, int y2, int z, int color, boolean queue) {
        DrawContext.drawRect(Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR), x1, y1, x2, y2, z, color, queue);
    }

    public static void drawRect(BufferBuilder builder, int x1, int y1, int x2, int y2, int z, boolean queue) {
        DrawContext.drawRect(builder, x1, y1, x2, y2, z, 0xFFFFFFFF, queue);
    }

    /** The color is specified with the ARGB format! */
    public static void drawRect(BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int color, boolean queue) {
        float[] c = Utils.getArgbColors0To1(color);

        builder.vertex(x2, y2, z).color(c[0], c[1], c[2], c[3]);
        builder.vertex(x1, y2, z).color(c[0], c[1], c[2], c[3]);
        builder.vertex(x1, y1, z).color(c[0], c[1], c[2], c[3]);
        builder.vertex(x1, y1, z).color(c[0], c[1], c[2], c[3]);
        builder.vertex(x2, y1, z).color(c[0], c[1], c[2], c[3]);
        builder.vertex(x2, y2, z).color(c[0], c[1], c[2], c[3]);
        if (queue) builder.addToQueue();
    }
    // endregion

    // region Texture
    public static void drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z, boolean queue) {
        var builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR_TEXTURE);
        DrawContext.drawTexture(builder, texture, x1, y1, x2, y2, z, 0, 0, queue);
    }

    public static void drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z, float u, float v, boolean queue) {
        var builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR_TEXTURE);
        DrawContext.drawTexture(builder, texture, x1, y1, x2, y2, z, u, v, 1, 1, queue);
    }

    public static void drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z, float u, float v, int texWidth, int texHeight, boolean queue) {
        var builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR_TEXTURE);
        DrawContext.drawTexture(builder, texture, x1, y1, x2, y2, z, u, v, texWidth, texHeight, 0xFFFFFFFF, queue);
    }

    public static void drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z, float u, float v, int texWidth, int texHeight, int color, boolean queue) {
        var builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR_TEXTURE);
        float u2 = u + 1;
        float v2 = v + 1;
        if (texWidth != 1 && texHeight != 1) {
            u2 = u + (float) (x2 - x1) / texWidth;
            v2 = v + (float) (y2 - y1) / texHeight;
        }

        DrawContext.drawTexture(builder, texture, x1, y1, x2, y2, z, u, v, u2, v2, color, queue, false);
    }

    public static void drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z, float u, float v, float u2, float v2,
                                   int color, boolean queue, boolean ignored) {
        var builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR_TEXTURE);
        float[] c = Utils.getArgbColors0To1(color);

        builder.vertex(x2, y2, z).color(c[0], c[1], c[2], c[3]).texture(u2, v2, texture);
        builder.vertex(x1, y2, z).color(c[0], c[1], c[2], c[3]).texture(u, v2, texture);
        builder.vertex(x1, y1, z).color(c[0], c[1], c[2], c[3]).texture(u, v, texture);
        builder.vertex(x1, y1, z).color(c[0], c[1], c[2], c[3]).texture(u, v, texture);
        builder.vertex(x2, y1, z).color(c[0], c[1], c[2], c[3]).texture(u2, v, texture);
        builder.vertex(x2, y2, z).color(c[0], c[1], c[2], c[3]).texture(u2, v2, texture);
        if (queue) builder.addToQueue();
    }

    // AA
    public static void drawTexture(BufferBuilder builder, Identifier texture, int x1, int y1, int x2, int y2, int z, boolean queue) {
        DrawContext.drawTexture(builder, texture, x1, y1, x2, y2, z, 0, 0, queue);
    }

    public static void drawTexture(BufferBuilder builder, Identifier texture, int x1, int y1, int x2, int y2, int z, float u, float v, boolean queue) {
        DrawContext.drawTexture(builder, texture, x1, y1, x2, y2, z, u, v, 1, 1, queue);
    }

    public static void drawTexture(BufferBuilder builder, Identifier texture, int x1, int y1, int x2, int y2, int z, float u, float v, int texWidth, int texHeight, boolean queue) {
        DrawContext.drawTexture(builder, texture, x1, y1, x2, y2, z, u, v, texWidth, texHeight, 0xFFFFFFFF, queue);
    }

    public static void drawTexture(BufferBuilder builder, Identifier texture, int x1, int y1, int x2, int y2, int z, float u, float v, int texWidth, int texHeight, int color, boolean queue) {
        float u2 = u + 1;
        float v2 = v + 1;
        if (texWidth != 1 && texHeight != 1) {
            u2 = u + (float) (x2 - x1) / texWidth;
            v2 = v + (float) (y2 - y1) / texHeight;
        }

        DrawContext.drawTexture(builder, texture, x1, y1, x2, y2, z, u, v, u2, v2, color, queue, false);
    }

    public static void drawTexture(BufferBuilder builder, Identifier texture, int x1, int y1, int x2, int y2, int z, float u, float v, float u2, float v2,
                                   int color, boolean queue, boolean ignored) {
        float[] c = Utils.getArgbColors0To1(color);

        builder.vertex(x2, y2, z).color(c[0], c[1], c[2], c[3]).texture(u2, v2, texture);
        builder.vertex(x1, y2, z).color(c[0], c[1], c[2], c[3]).texture(u, v2, texture);
        builder.vertex(x1, y1, z).color(c[0], c[1], c[2], c[3]).texture(u, v, texture);
        builder.vertex(x1, y1, z).color(c[0], c[1], c[2], c[3]).texture(u, v, texture);
        builder.vertex(x2, y1, z).color(c[0], c[1], c[2], c[3]).texture(u2, v, texture);
        builder.vertex(x2, y2, z).color(c[0], c[1], c[2], c[3]).texture(u2, v2, texture);
        if (queue) builder.addToQueue();
    }
    // endregion

    // region Gradient
    public static void drawGradient(int x1, int y1, int x2, int y2, int z, GradientDirection direction, int startColor, int endColor, boolean queue) {
        var builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
        DrawContext.drawGradient(builder, x1, y1, x2, y2, z, direction, startColor, endColor, queue);
    }

    public static void drawGradient(BufferBuilder builder, int x1, int y1, int x2, int y2, int z, GradientDirection direction, int startColor, int endColor, boolean queue) {
        float[] sc = Utils.getArgbColors0To1(startColor);
        float[] ec = Utils.getArgbColors0To1(endColor);

        switch (direction == null ? GradientDirection.DEFAULT : direction) {
            case TOP_TO_BOTTOM -> {
                builder.vertex(x2, y2, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x1, y2, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x1, y1, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x1, y1, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x2, y1, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x2, y2, z).color(sc[0], sc[1], sc[2], sc[3]);
            }
            case BOTTOM_TO_TOP -> {
                builder.vertex(x2, y2, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x1, y2, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x1, y1, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x1, y1, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x2, y1, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x2, y2, z).color(ec[0], ec[1], ec[2], ec[3]);
            }
            case LEFT_TO_RIGHT -> {
                builder.vertex(x2, y2, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x1, y2, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x1, y1, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x1, y1, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x2, y1, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x2, y2, z).color(ec[0], ec[1], ec[2], ec[3]);
            }
            case RIGHT_TO_LEFT -> {
                builder.vertex(x2, y2, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x1, y2, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x1, y1, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x1, y1, z).color(ec[0], ec[1], ec[2], ec[3]);
                builder.vertex(x2, y1, z).color(sc[0], sc[1], sc[2], sc[3]);
                builder.vertex(x2, y2, z).color(sc[0], sc[1], sc[2], sc[3]);
            }
        }
        if (queue) builder.addToQueue();
    }
    // endregion

}
