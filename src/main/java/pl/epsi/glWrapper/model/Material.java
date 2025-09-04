package pl.epsi.glWrapper.model;

import org.joml.Vector4f;
import pl.epsi.glWrapper.utils.Identifier;
import pl.epsi.glWrapper.utils.Texture;
import pl.epsi.glWrapper.utils.TexturePool;

public class Material {

    private Vector4f color = new Vector4f(1, 1, 1, 1);
    private float metallic = 0;
    private float roughness = 0;
    private Vector4f emissiveColor = new Vector4f(1, 1, 1, 1);
    private Texture diffuseMap = TexturePool.getTexture(new Identifier("textures/unknown.png"));

    public Texture getDiffuseMap() {
        return diffuseMap;
    }

    public void setDiffuseMap(Texture diffuseMap) {
        this.diffuseMap = diffuseMap;
    }

    public Vector4f getColor() {
        return color;
    }

    public Material setColor(Vector4f color) {
        this.color = color;
        return this;
    }

    public float getMetallic() {
        return metallic;
    }

    public Material setMetallic(float metallic) {
        this.metallic = metallic;
        return this;
    }

    public float getRoughness() {
        return roughness;
    }

    public Material setRoughness(float roughness) {
        this.roughness = roughness;
        return this;
    }

    public Vector4f getEmissiveColor() {
        return emissiveColor;
    }

    public Material setEmissiveColor(Vector4f emissiveColor) {
        this.emissiveColor = emissiveColor;
        return this;
    }
}
