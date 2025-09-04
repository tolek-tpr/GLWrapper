package pl.epsi.glWrapper.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import pl.epsi.glWrapper.callbacks.Keyboard;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.utils.Time;

public class Camera {

    private static final float YAW = -90f;
    private static final float PITCH = 0f;
    private static final float MOVEMENT_SPEED = 500f;
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float ZOOM = 45.0f;

    private final Vector3f position;
    private final Vector3f worldUp;

    private Vector3f front;
    private Vector3f up;
    private Vector3f right;

    float yaw, pitch;
    float movementSpeed, mouseSensitivity, zoom;

    public Camera() {
        this(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), YAW, PITCH, new Vector3f(0, 0, -1),
                MOVEMENT_SPEED, MOUSE_SENSITIVITY, ZOOM);
    }

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch) {
        this(position, up, yaw, pitch, new Vector3f(0, 0, -1), MOVEMENT_SPEED, MOUSE_SENSITIVITY, ZOOM);
    }

    public Camera(Vector3f position, Vector3f up, float yaw, float pitch, Vector3f front, float movementSpeed, float mouseSensitivity, float zoom) {
        this.position = position;
        this.up = up;
        this.worldUp = up;
        this.yaw = yaw;
        this.pitch = pitch;
        this.front = front;
        this.movementSpeed = movementSpeed;
        this.mouseSensitivity = mouseSensitivity;
        this.zoom = zoom;

        updateCameraVectors();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public void tick(long window) {
        Renderer.viewMatrix = this.getViewMatrix();
        this.cameraMovementHandler(window);
    }

    public void cameraMovementHandler(long window) {
        if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_W)) {
            this.cameraMovementHandler(CameraMovement.FORWARD, Time.getDeltaTime());
        } else if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_S)) {
            this.cameraMovementHandler(CameraMovement.BACKWARD, Time.getDeltaTime());
        } else if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_A)) {
            this.cameraMovementHandler(CameraMovement.LEFT, Time.getDeltaTime());
        } else if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_D)) {
            this.cameraMovementHandler(CameraMovement.RIGHT, Time.getDeltaTime());
        } else if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_SPACE)) {
            this.cameraMovementHandler(CameraMovement.UP, Time.getDeltaTime());
        } else if (Keyboard.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT)) {
            this.cameraMovementHandler(CameraMovement.DOWN, Time.getDeltaTime());
        }
    }

    public void cameraMovementHandler(CameraMovement direction, float deltaTime) {
        Vector3f front = new Vector3f(this.front);
        Vector3f right = new Vector3f(this.right);
        Vector3f worldUp = new Vector3f(this.worldUp);
        float velocity = movementSpeed * deltaTime;
        if (direction == CameraMovement.FORWARD)
            position.add(front.mul(velocity));
        if (direction == CameraMovement.BACKWARD)
            position.sub(front.mul(velocity));
        if (direction == CameraMovement.LEFT)
            position.sub(right.mul(velocity));
        if (direction == CameraMovement.RIGHT)
            position.add(right.mul(velocity));
        if (direction == CameraMovement.UP)
            position.add(worldUp.mul(velocity));
        if (direction == CameraMovement.DOWN)
            position.sub(worldUp.mul(velocity));
    }

    public void cameraMouseMoveCallback(double xOffset, double yOffset) {
        cameraMouseMoveCallback((float) xOffset, (float) yOffset, true);
    }

    public void cameraMouseMoveCallback(float xOffset, float yOffset, boolean constrainPitch) {
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;

        yaw += xOffset;
        pitch += yOffset;

        if (constrainPitch) { pitch = Math.max(-89f, Math.min(89, pitch)); }
        updateCameraVectors();
    }

    public void cameraMouseScrollCallback(double xOffset, double yOffset) {
        this.cameraMouseScrollCallback((float) yOffset);
    }

    public void cameraMouseScrollCallback(float yOffset) {
        zoom -= yOffset;
        zoom = Math.max(1, Math.min(45, zoom));
    }

    private void updateCameraVectors() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        Vector3f newFront = new Vector3f();
        newFront.x = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        newFront.y = (float) (Math.sin(pitchRad));
        newFront.z = (float) (Math.sin(yawRad) * Math.cos(pitchRad));

        this.front = newFront.normalize();
        this.right = new Vector3f(front).cross(worldUp).normalize();
        this.up = new Vector3f(right).cross(front).normalize();
    }

    public Vector3f getPos() { return this.position; }

    public enum CameraMovement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        UP,
        DOWN
    }

}
