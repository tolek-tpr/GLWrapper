package pl.epsi.glWrapper.utils;

import org.lwjgl.opengl.GL30;

public enum GlNumberType {

    BYTE(Byte.class, GL30.GL_BYTE),
    SHORT(Short.class, GL30.GL_SHORT),
    INT(Integer.class, GL30.GL_INT),
    FLOAT(Float.class, GL30.GL_FLOAT),
    DOUBLE(Double.class, GL30.GL_DOUBLE);

    private final Class<?> clazz;
    private final int gl_type;

    GlNumberType(Class<?> clazz, int gl_type) {
        this.clazz = clazz;
        this.gl_type = gl_type;
    }

    public static GlNumberType fromGlInt(int gl_type) {
        for (GlNumberType type : values()) {
            if (type.gl_type == gl_type) return type;
        }

        throw new IllegalArgumentException("Unknown GlNumberType for GL Type: " + gl_type);
    }

    public static boolean shouldUseIPointer(GlNumberType type) {
        return type == INT;
    }

    public static int toGlInt(GlNumberType type) {
        return type.gl_type;
    }

    public Class<?> getClazz() { return this.clazz; }
    public int getGlType() { return this.gl_type; }

    public int getSizeInBytes() {
        try {
            return clazz.getField("BYTES").getInt(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Could not get BYTES field from " + clazz.getSimpleName(), e);
        }
    }

}
