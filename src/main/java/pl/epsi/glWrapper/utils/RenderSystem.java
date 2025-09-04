package pl.epsi.glWrapper.utils;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import pl.epsi.glWrapper.render.Renderer;

public class RenderSystem {

    private static boolean is3D = false;
    private static int maxTextureAmount = 8;
    private static int windowWidth, windowHeight;
    private static boolean renderFullBuffersImmediately = false;

    public static void set3D(boolean is3D) {
        if (windowWidth == 0 || windowHeight == 0) {
            int[] viewportData = new int[4];
            GL30.glGetIntegerv(GL11.GL_VIEWPORT, viewportData);
            windowWidth = viewportData[2];
            windowHeight = viewportData[3];
        }

        RenderSystem.set3D(windowWidth, windowHeight, is3D);
    }

    public static void set3D(int windowWidth, int windowHeight, boolean is3D) {
        Renderer.updateProjMatrix(windowWidth, windowHeight, is3D);
        RenderSystem.is3D = is3D;
    }

    public static boolean is3D() { return RenderSystem.is3D; }

    public static void setMaxTextures(int maxTextures) { RenderSystem.maxTextureAmount = maxTextures; }
    public static int getMaxTextures() { return RenderSystem.maxTextureAmount; }

    public static void setViewport(int x, int y, int width, int height) {
        windowWidth = width;
        windowHeight = height;
        GL30.glViewport(x, y, width, height);
    }

    public static boolean shouldRenderFullBuffersImmediately() {
        return renderFullBuffersImmediately;
    }

    public static void setRenderFullBuffersImmediately(boolean renderFullBuffersImmediately) {
        RenderSystem.renderFullBuffersImmediately = renderFullBuffersImmediately;
    }
}
