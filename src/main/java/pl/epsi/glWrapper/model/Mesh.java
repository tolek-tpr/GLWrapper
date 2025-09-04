package pl.epsi.glWrapper.model;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;

public class Mesh {

    private final ArrayList<Vector3f> vertices = new ArrayList<>();
    private final ArrayList<Vector3f> normals = new ArrayList<>();
    private final ArrayList<Vector2f> uvs = new ArrayList<>();
    private final ArrayList<Integer> indices = new ArrayList<>();
    private final Material material = new Material();

    public final Matrix4f modelMatrix;

    public Mesh(ArrayList<Vector3f> vertices, ArrayList<Vector3f> normals, ArrayList<Vector2f> uvs, ArrayList<Integer> indices) {
        this(vertices, normals, uvs, indices, new Matrix4f().identity());
    }

    public Mesh(ArrayList<Vector3f> vertices, ArrayList<Vector3f> normals, ArrayList<Vector2f> uvs, ArrayList<Integer> indices,
                Matrix4f modelMatrix) {
        this.vertices.addAll(vertices);
        this.normals.addAll(normals);
        this.uvs.addAll(uvs);
        this.indices.addAll(indices);
        this.modelMatrix = modelMatrix;
    }

    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    public ArrayList<Vector3f> getNormals() {
        return normals;
    }

    public ArrayList<Vector2f> getUvs() {
        return uvs;
    }

    public ArrayList<Integer> getIndices() {
        return indices;
    }

    public Material getMaterial() { return this.material; }

    public Mesh copy() {
        return new Mesh(this.vertices, this.normals, this.uvs, this.indices, new Matrix4f(this.modelMatrix));
    }

}
