package pl.epsi.glWrapper.utils;

import java.util.HashMap;

public class TexturePool {

    private static final HashMap<Identifier, Texture> textures = new HashMap<>();

    public static Texture getTexture(Identifier id) {
        if (textures.containsKey(id)) {
            return textures.get(id);
        } else {
            Texture tex = new Texture(id);
            textures.put(id, tex);
            return tex;
        }
    }

}
