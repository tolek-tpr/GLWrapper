package pl.epsi.glWrapper.utils;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Texture {

    private final int ID;
    private int width, height;

    protected Texture(Identifier identifier) {
        STBImage.stbi_set_flip_vertically_on_load(true);

        this.ID = GL20.glGenTextures();
        GL20.glBindTexture(GL11.GL_TEXTURE_2D, ID);

        GL20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

        GL20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        ByteBuffer image = loadImage(identifier, width, height, channels);

        if (image != null) {
            this.width = width.get(0);
            this.height = height.get(0);
            if (channels.get(0) == 3) {
                GL20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, width.get(0), height.get(0), 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, image);
            } else if (channels.get(0) == 4) {
                GL20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(0), height.get(0), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, image);
            } else {
                assert false : "Error while loading texture, unknown number of channels = " + channels.get(0);
            }
        } else {
            assert false : "Texture not loaded! Filepath: " + identifier.toString();
        }

        STBImage.stbi_image_free(image);
    }

    public static ByteBuffer loadImage(Identifier ident, IntBuffer width, IntBuffer height, IntBuffer channels) {
        ByteBuffer imageBuffer;

        try {
            byte[] bytes = ident.asInputStream().readAllBytes();
            imageBuffer = ByteBuffer.allocateDirect(bytes.length).put(bytes);
            imageBuffer.flip();

            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 0);
            if (image == null) {
                throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
            }

            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void use() {
        GL20.glBindTexture(GL11.GL_TEXTURE_2D, ID);
    }

    public void unbind() {
        GL20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }

}
