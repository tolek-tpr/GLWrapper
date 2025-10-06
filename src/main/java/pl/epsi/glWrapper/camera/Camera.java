package pl.epsi.glWrapper.camera;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import pl.epsi.glWrapper.callbacks.Keyboard;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.utils.Time;

public class Camera {

    private static final float MOVEMENT_SPEED = 500f;
    private static final float ZOOM_SPEED = 1f;

    private final Vector2f position;
    private float zoom;

    private float movementSpeed;

    public Camera() {
        this(new Vector2f(0, 0), 1f, MOVEMENT_SPEED);
    }

    public Camera(Vector2f position, float zoom, float movementSpeed) {
        this.position = position;
        this.zoom = zoom;
        this.movementSpeed = movementSpeed;
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .translate(new Vector3f(-position.x, -position.y, 0))
                .scale(new Vector3f(zoom, zoom, 1f));
    }

    public void tick(long window) {
        Renderer.viewMatrix = this.getViewMatrix();
        handleMovement(window, Time.getDeltaTime());
    }

    private void handleMovement(long window, float deltaTime) {
        float velocity = movementSpeed * deltaTime;
        if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)) position.y += velocity;
        if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_SPACE)) position.y -= velocity;
        if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_D)) position.x -= velocity;
        if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_A)) position.x += velocity;
    }

    public void handleScroll(double xOffset, double yOffset) {
        zoom += (float) (yOffset * ZOOM_SPEED);
        zoom = Math.max(0.1f, zoom);
    }

    public Vector2f getPos() {
        return position;
    }

}

