package pl.epsi.glWrapper.utils;

public class Profiler {

    private static long frameStart;
    private static long sectionStart;
    private static String currentSection;

    public static void startFrame() {
        frameStart = System.nanoTime();
        System.out.println("----- Frame Start -----");
    }

    public static void startSection(String name) {
        sectionStart = System.nanoTime();
        currentSection = name;
    }

    public static void endSection() {
        long now = System.nanoTime();
        long duration = now - sectionStart;
        System.out.printf("Section %-15s: %.3f ms%n", currentSection, duration / 1_000_000.0);
        currentSection = null;
    }

    public static void endFrame() {
        long now = System.nanoTime();
        long frameTime = now - frameStart;
        double ms = frameTime / 1_000_000.0;
        double fps = 1000.0 / ms;

        System.out.printf("Frame Time: %.3f ms (%.1f FPS)%n", ms, fps);
        System.out.println("------------------------");
    }

}
