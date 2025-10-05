package pl.epsi.glWrapper.buffers.gpu;

import org.lwjgl.opengl.GL45;

import java.nio.ByteBuffer;

public class MappableGpuBuffer extends GpuBuffer {

    private final ByteBuffer mappedMemory;

    public MappableGpuBuffer(BufferUsage usage, BufferTarget target, int size) {
        super(usage, target);

        this.bind();
        GL45.glBufferStorage(getBufferTarget().getGlTarget(), size,
                GL45.GL_MAP_WRITE_BIT | GL45.GL_MAP_PERSISTENT_BIT | GL45.GL_MAP_COHERENT_BIT);

        mappedMemory = GL45.glMapBufferRange(getBufferTarget().getGlTarget(), 0, size,
                GL45.GL_MAP_WRITE_BIT | GL45.GL_MAP_PERSISTENT_BIT | GL45.GL_MAP_COHERENT_BIT, null);
    }

    public ByteBuffer getBuffer() { return this.mappedMemory; }

    public void clear() { this.mappedMemory.clear(); }

    @Override
    public void delete() {
        this.bind();
        GL45.glUnmapBuffer(this.getBufferTarget().getGlTarget());
        GL45.glDeleteBuffers(this.getId());
    }

}
