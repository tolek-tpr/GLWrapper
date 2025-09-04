package pl.epsi.glWrapper.callbacks;

import pl.epsi.glWrapper.functional.IntBinaryConsumer;

import java.util.ArrayList;

public class Viewport {

    private static final ArrayList<IntBinaryConsumer> viewportSizeChangeCallbacks = new ArrayList<>();

    public static void onViewportSizeChange(long window, int width, int height) {
        viewportSizeChangeCallbacks.forEach(t -> t.applyAsInt(width, height));
    }

    // region Registering Callbacks
    public static void registerViewportSizeChangeCallback(IntBinaryConsumer c) { Viewport.viewportSizeChangeCallbacks.add(c); }
    // endregion

}
