package pl.epsi.glWrapper.shader;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import pl.epsi.glWrapper.Config;
import pl.epsi.glWrapper.utils.Texture;

import java.util.ArrayList;

public class TextureSamplerUniformProvider implements UniformProvider {

    private final ArrayList<Texture> textures;

    public TextureSamplerUniformProvider(ArrayList<Texture> textures) {
        this.textures = textures;
    }

    @Override
    public void apply(ShaderProgram program) {
        for (int i = 0; i < Math.min(textures.size(), Config.TEXTURE_AMOUNT); i++) {
            GL30.glActiveTexture(GL13.GL_TEXTURE0 + i);
            textures.get(i).use();
        }

        int[] samplers = new int[Config.TEXTURE_AMOUNT];
        for (int i = 0; i < Config.TEXTURE_AMOUNT; i++) {
            samplers[i] = i;
        }

        program.uniformIntArray("uTextures", samplers);
    }

}
