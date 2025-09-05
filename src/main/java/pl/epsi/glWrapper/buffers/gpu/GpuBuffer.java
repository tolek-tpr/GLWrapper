package pl.epsi.glWrapper.buffers.gpu;

import org.lwjgl.opengl.GL45;

import java.nio.ByteBuffer;

public class GpuBuffer {

    private final int id;
    private final BufferTarget bufferTarget;
    private final BufferUsage bufferUsage;

    private int currentSize = 0;

    public GpuBuffer(BufferTarget bufferTarget, BufferUsage bufferUsage) {
        this.id = GL45.glGenBuffers();
        this.bufferTarget = bufferTarget;
        this.bufferUsage = bufferUsage;
    }

    public void bind() {
        GL45.glBindBuffer(bufferTarget.getGlTarget(), id);
    }

    public void unbind() {
        GL45.glBindBuffer(bufferTarget.getGlTarget(), 0);
    }

    /**
     * Uploads data into the buffer (overwrites previous content).
     */
    public void upload(ByteBuffer data, int sizeBytes) {
        bind();
        GL45.glBufferData(bufferTarget.getGlTarget(), data, bufferUsage.getGlUsage());
        this.currentSize = sizeBytes;
    }

    /**
     * Updates a sub-region of the buffer (requires buffer already allocated).
     */
    public void update(long offset, ByteBuffer data) {
        bind();
        GL45.glBufferSubData(bufferTarget.getGlTarget(), offset, data);
    }

    public void delete() {
        GL45.glDeleteBuffers(id);
    }

    public int getId() {
        return id;
    }

    public BufferTarget getBufferTarget() {
        return bufferTarget;
    }

    public BufferUsage getBufferUsage() {
        return bufferUsage;
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
