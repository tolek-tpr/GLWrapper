package pl.epsi.glWrapper.model;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.assimp.*;
import pl.epsi.glWrapper.utils.Identifier;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class ModelLoader {

    private static AIScene loadScene(Identifier id) {
        try {
            InputStream in = id.asInputStream();
            if (in == null) throw new FileNotFoundException("Resource not found: " + id);

            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
            buffer.put(bytes).flip();

            // Import from memory
            AIScene scene = Assimp.aiImportFileFromMemory(buffer, Assimp.aiProcess_Triangulate | Assimp.aiProcess_JoinIdenticalVertices, (String) null);
            if (scene == null) {
                throw new RuntimeException("Assimp failed: " + Assimp.aiGetErrorString());
            }
            return scene;
        } catch (IOException e) {
            throw new IllegalArgumentException("Resource not found: " + id);
        }
    }

    public static Mesh loadSingleMesh(Identifier id) {
        AIScene scene = loadScene(id);

        ArrayList<Vector3f> verticesList = new ArrayList<>();
        ArrayList<Vector3f> normalsList  = new ArrayList<>();
        ArrayList<Vector2f> texCoordsList = new ArrayList<>();
        ArrayList<Integer> indicesList    = new ArrayList<>();

        int vertexOffset = 0;

        for (int m = 0; m < scene.mNumMeshes(); m++) {
            AIMesh mesh = AIMesh.create(scene.mMeshes().get(m));

            // Vertices
            AIVector3D.Buffer verts = mesh.mVertices();
            for (int i = 0; i < mesh.mNumVertices(); i++) {
                AIVector3D v = verts.get(i);
                verticesList.add(new Vector3f(v.x(), v.y(), v.z()));
            }

            // Normals
            if (mesh.mNormals() != null) {
                AIVector3D.Buffer norms = mesh.mNormals();
                for (int i = 0; i < mesh.mNumVertices(); i++) {
                    AIVector3D n = norms.get(i);
                    normalsList.add(new Vector3f(n.x(), n.y(), n.z()));
                }
            }

            // Texture coords (take first channel)
            if (mesh.mTextureCoords(0) != null) {
                AIVector3D.Buffer uvs = mesh.mTextureCoords(0);
                for (int i = 0; i < mesh.mNumVertices(); i++) {
                    AIVector3D uv = uvs.get(i);
                    texCoordsList.add(new Vector2f(uv.x(), uv.y()));
                }
            }

            // Faces (indices)
            AIFace.Buffer faces = mesh.mFaces();
            for (int i = 0; i < mesh.mNumFaces(); i++) {
                AIFace face = faces.get(i);
                IntBuffer indices = face.mIndices();
                for (int j = 0; j < face.mNumIndices(); j++) {
                    int idx = indices.get(j) + vertexOffset;
                    indicesList.add(idx);
                }
            }

            vertexOffset += mesh.mNumVertices();
        }

        Assimp.aiReleaseImport(scene);

        return new Mesh(verticesList, normalsList, texCoordsList, indicesList);
    }

}
