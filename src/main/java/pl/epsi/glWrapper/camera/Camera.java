package pl.epsi.glWrapper.camera;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import pl.epsi.glWrapper.utils.Time;

public class Camera {

    private enum CameraMovement {
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT
    }

    private final Vector3f position;
    private final Vector3f rotation;
    private double lastPosX, lastPosY;

    private final Matrix4f viewMatrix;

    public static float SPEED = 1000;
    public static float SENSITIVITY = 0.5f;

    public Camera() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.viewMatrix = new Matrix4f();
    }

    public Camera(Vector3f position, Vector3f rotation) {
        this.position = new Vector3f(position);
        this.rotation = new Vector3f(rotation);
        this.viewMatrix = new Matrix4f();
    }

    public Matrix4f getViewMatrix() {
        viewMatrix.identity();

        viewMatrix.rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z));

        viewMatrix.translate(-position.x, -position.y, -position.z);

        return viewMatrix;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.rotation.set(pitch, yaw, roll);
    }

    public void move(float dx, float dy, float dz) {
        position.add(dx, dy, dz);
    }

    public void rotate(float dx, float dy, float dz) {
        rotation.add(dx, dy, dz);
    }

    public void moveForward(float amount) {
        Vector3f forward = new Vector3f();
        getForward(forward);
        position.add(forward.mul(amount, new Vector3f()));
    }

    public void moveBackward(float amount) {
        moveForward(-amount);
    }

    public void moveRight(float amount) {
        Vector3f right = new Vector3f();
        getRight(right);
        position.add(right.mul(amount, new Vector3f()));
    }

    public void moveLeft(float amount) {
        moveRight(-amount);
    }

    private void getForward(Vector3f dest) {
        // Calculate forward direction from pitch & yaw
        dest.x = (float) (Math.sin(Math.toRadians(rotation.y)) * Math.cos(Math.toRadians(rotation.x)));
        dest.y = (float) -Math.sin(Math.toRadians(rotation.x));
        dest.z = (float) ((float) -Math.cos(Math.toRadians(rotation.y)) * Math.cos(Math.toRadians(rotation.x)));
        dest.normalize();
    }

    private void getRight(Vector3f dest) {
        Vector3f forward = new Vector3f();
        getForward(forward);
        forward.cross(new Vector3f(0, 1, 0), dest).normalize();
    }

    public void rotate(float deltaX, float deltaY) {
        rotation.y += deltaX * SENSITIVITY;
        rotation.x += deltaY * SENSITIVITY;

        // Clamp pitch to avoid flipping
        rotation.x = Math.max(-89f, Math.min(89f, rotation.x));
    }

    public void onKey(long window, int keyCode, int scanCode, int mods) {
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS) this.moveForward(SPEED * Time.getDeltaTime());
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS) this.moveBackward(SPEED * Time.getDeltaTime());
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS) this.moveLeft(SPEED * Time.getDeltaTime());
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS) this.moveRight(SPEED * Time.getDeltaTime());
    }

    public void onMouse(long window, double xPos, double yPos) {
        float mouseDX = (float) (xPos - lastPosX);
        float mouseDY = (float) (yPos - lastPosY);
        lastPosX = xPos;
        lastPosY = yPos;

        this.rotate(mouseDX, mouseDY);
    }

}
