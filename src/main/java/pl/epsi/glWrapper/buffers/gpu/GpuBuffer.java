package pl.epsi.glWrapper.buffers.gpu;

import org.lwjgl.opengl.GL45;

import java.nio.ByteBuffer;

public class GpuBuffer {

    private final int id;
    private final BufferUsage usage;
    private final BufferTarget target;

    private int currentSize = 0;

    public GpuBuffer(BufferUsage usage, BufferTarget target) {
        this.id = GL45.glGenBuffers();
        this.usage = usage;
        this.target = target;
    }

    public void bind() {
        GL45.glBindBuffer(target.getGlTarget(), id);
    }

    public void unbind() {
        GL45.glBindBuffer(target.getGlTarget(), 0);
    }

    public void upload(ByteBuffer data, int sizeBytes) {
        bind();
        GL45.glBufferData(target.getGlTarget(), data, usage.getGlUsage());
        this.currentSize = sizeBytes;
    }

    public void update(long offset, ByteBuffer data) {
        bind();
        GL45.glBufferSubData(target.getGlTarget(), offset, data);
    }

    public void delete() {
        GL45.glDeleteBuffers(id);
    }

    public int getId() {
        return id;
    }

    public BufferTarget getBufferTarget() {
        return target;
    }

    public BufferUsage getBufferUsage() {
        return usage;
    }

    public int getCurrentSizeBytes() {
        return currentSize;
    }

    public static enum BufferTarget {

        ARRAY_BUFFER(GL45.GL_ARRAY_BUFFER),
        ELEMENT_ARRAY_BUFFER(GL45.GL_ELEMENT_ARRAY_BUFFER),
        SHADER_STORAGE_BUFFER(GL45.GL_SHADER_STORAGE_BUFFER),
        UNIFORM_BUFFER(GL45.GL_UNIFORM_BUFFER);

        private final int glTarget;

        BufferTarget(int glTarget) {
            this.glTarget = glTarget;
        }

        public int getGlTarget() { return this.glTarget; }

    }


    public static enum BufferUsage {

        STATIC_DRAW(GL45.GL_STATIC_DRAW),
        STATIC_READ(GL45.GL_STATIC_READ),
        STATIC_COPY(GL45.GL_STATIC_COPY),

        DYNAMIC_DRAW(GL45.GL_DYNAMIC_DRAW),
        DYNAMIC_READ(GL45.GL_DYNAMIC_READ),
        DYNAMIC_COPY(GL45.GL_DYNAMIC_COPY),

        STREAM_DRAW(GL45.GL_STREAM_DRAW),
        STREAM_READ(GL45.GL_STREAM_READ),
        STREAM_COPY(GL45.GL_STREAM_COPY);

        private final int glUsage;

        BufferUsage(int glUsage) {
            this.glUsage = glUsage;
        }

        public int getGlUsage() { return this.glUsage; }

    }

}
