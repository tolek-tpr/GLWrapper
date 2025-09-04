package pl.epsi.glWrapper.shader;

import pl.epsi.glWrapper.buffers.DrawMode;
import pl.epsi.glWrapper.utils.Identifier;

public class ShaderProgramKeys {

    // TODO: Add shaders for all vertex formats!
    public static final ShaderProgram POSITION_PROGRAM = new ShaderProgram(new Identifier("shaders/core/position_program.vsh"),
            new Identifier("shaders/core/position_program.fsh"));
    public static final ShaderProgram POSITION_COLOR_PROGRAM = new ShaderProgram(new Identifier("shaders/core/position_color_program.vsh"),
            new Identifier("shaders/core/position_color_program.fsh"));
    public static final ShaderProgram POSITION_TEXTURE_PROGRAM = new ShaderProgram(new Identifier("shaders/core/position_texture_program.vsh"),
            new Identifier("shaders/core/position_texture_program.fsh"));
    public static final ShaderProgram POSITION_COLOR_TEXTURE_PROGRAM = new ShaderProgram(new Identifier("shaders/core/position_color_texture_program.vsh"),
            new Identifier("shaders/core/position_color_texture_program.fsh"));


    // 3D
    public static final ShaderProgram POSITION_TEXTURE_NORMAL_PROGRAM_3D = new ShaderProgram(new Identifier("shaders/core/3d/position_texture_normal.vsh"),
            new Identifier("shaders/core/3d/position_texture_normal.fsh"));

    public static ShaderProgram getByVertexFormat3D(DrawMode.VertexFormat format) {
        return switch (format) {
            case POSITION_TEXTURE_NORMAL -> POSITION_TEXTURE_NORMAL_PROGRAM_3D;
            default -> throw new IllegalArgumentException("Unknown shader for VertexFormat: " + format);
        };
    }

    public static ShaderProgram getByVertexFormat(DrawMode.VertexFormat format) {
        return switch (format) {
            case POSITION -> POSITION_PROGRAM;
            case POSITION_COLOR -> POSITION_COLOR_PROGRAM;
            case POSITION_TEXTURE -> POSITION_TEXTURE_PROGRAM;
            case POSITION_COLOR_TEXTURE -> POSITION_COLOR_TEXTURE_PROGRAM;
            default -> throw new IllegalArgumentException("Unknown shader for VertexFormat: " + format);
        };
    }

    public static Identifier getVertexShaderByVertexFormat(DrawMode.VertexFormat format) {
        return switch (format) {
            case POSITION -> new Identifier("shaders/core/position_program.vsh");
            case POSITION_COLOR -> new Identifier("shaders/core/position_color_program.vsh");
            case POSITION_TEXTURE -> new Identifier("shaders/core/position_texture_program.vsh");
            case POSITION_COLOR_TEXTURE -> new Identifier("shaders/core/position_color_texture_program.vsh");
            default -> throw new IllegalArgumentException("Unknown vertex shader for VertexFormat " + format);
        };
    }

}
