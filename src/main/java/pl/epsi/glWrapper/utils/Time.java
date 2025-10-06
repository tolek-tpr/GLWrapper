package pl.epsi.glWrapper.utils;

public class Time {

    private static long lastTime = System.nanoTime();
    private static float deltaTime = 0f;

    public static void onEndFrame() {
        long currentTime = System.nanoTime();
        deltaTime = (currentTime - lastTime) / 1_000_000_000f; // convert ns to seconds
        lastTime = currentTime;
    }

    public static float getDeltaTime() {
        return deltaTime;
    }

}
