package pl.epsi.glWrapper.utils;

import org.lwjgl.opengl.GL30;

import java.nio.ByteBuffer;

public enum GlNumberType {

    BYTE(Byte.class, GL30.GL_BYTE)       { public void put(ByteBuffer buf, Object val) {    buf.put((Byte) val);             } },
    SHORT(Short.class, GL30.GL_SHORT)    { public void put(ByteBuffer buf, Object val) {    buf.putShort((Short) val);       } },
    INT(Integer.class, GL30.GL_INT)      { public void put(ByteBuffer buf, Object val) {    buf.putInt((Integer) val);       } },
    FLOAT(Float.class, GL30.GL_FLOAT)    { public void put(ByteBuffer buf, Object val) {    buf.putFloat((Float) val);       } },
    DOUBLE(Double.class, GL30.GL_DOUBLE) { public void put(ByteBuffer buf, Object val) {    buf.putDouble((Double) val);     } };

    private final Class<?> clazz;
    private final int gl_type;

    GlNumberType(Class<?> clazz, int gl_type) {
        this.clazz = clazz;
        this.gl_type = gl_type;
    }

    public abstract void put(ByteBuffer buf, Object val);

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
