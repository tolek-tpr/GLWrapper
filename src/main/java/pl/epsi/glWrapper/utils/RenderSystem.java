package pl.epsi.glWrapper.utils;

import org.lwjgl.opengl.GL30;

public class RenderSystem {

    private static int maxTextureAmount = 8;
    private static int windowWidth, windowHeight;

    public static void setMaxTextures(int maxTextures) { RenderSystem.maxTextureAmount = maxTextures; }
    public static int getMaxTextures() { return RenderSystem.maxTextureAmount; }

    public static void setViewport(int x, int y, int width, int height) {
        windowWidth = width;
        windowHeight = height;
        GL30.glViewport(x, y, width, height);
    }

}
