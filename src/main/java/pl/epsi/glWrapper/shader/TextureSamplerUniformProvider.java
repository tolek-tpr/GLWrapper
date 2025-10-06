package pl.epsi.glWrapper.shader;

import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import pl.epsi.glWrapper.utils.RenderSystem;
import pl.epsi.glWrapper.utils.Texture;

import java.util.ArrayList;

public class TextureSamplerUniformProvider implements UniformProvider {

    private final ArrayList<Texture> textures;

    public TextureSamplerUniformProvider(ArrayList<Texture> textures) {
        this.textures = textures;
    }

    @Override
    public void apply(ShaderProgram program) {
        for (int i = 0; i < Math.min(textures.size(), RenderSystem.getMaxTextures()); i++) {
            GL30.glActiveTexture(GL13.GL_TEXTURE0 + i);
            textures.get(i).use();
        }

        int[] samplers = new int[RenderSystem.getMaxTextures()];
        for (int i = 0; i < RenderSystem.getMaxTextures(); i++) {
            samplers[i] = i;
        }

        program.uniformIntArray("uTextures", samplers);
    }

}
