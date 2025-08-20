package pl.epsi.glWrapper.shader;

import pl.epsi.glWrapper.render.Renderer;

public class ProjectionMatrixUniformProvider implements UniformProvider {

    @Override
    public void apply(ShaderProgram program) {
        program.uniformMat4f("projMatrix", Renderer.projMatrix);
    }

}
