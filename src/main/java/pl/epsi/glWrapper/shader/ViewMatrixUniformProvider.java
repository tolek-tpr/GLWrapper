package pl.epsi.glWrapper.shader;

import pl.epsi.glWrapper.render.Renderer;

public class ViewMatrixUniformProvider implements UniformProvider {

    @Override
    public void apply(ShaderProgram program) {
        program.uniformMat4f("viewMatrix", Renderer.viewMatrix);
    }

}
