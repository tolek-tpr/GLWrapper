package pl.epsi.glWrapper.buffers.gpu;

import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;

public class MappedGpuBuffer extends GpuBuffer {

    private final ByteBuffer mappedMemory;

    private final int frameCount;
    private final int frameSize;

    private int frameIndex = 0;

    public MappedGpuBuffer(BufferTarget bufferTarget, BufferUsage bufferUsage, int frameCount, int frameSize) {
        super(bufferTarget, bufferUsage);
        this.frameCount = frameCount;
        this.frameSize = frameSize;

        int totalSize = frameCount * frameSize;

        this.bind();
        GL45.glBufferStorage(bufferTarget.getGlTarget(), totalSize,
                GL45.GL_MAP_WRITE_BIT | GL45.GL_MAP_PERSISTENT_BIT | GL45.GL_MAP_COHERENT_BIT);

        mappedMemory = GL45.glMapBufferRange(bufferTarget.getGlTarget(), 0, totalSize,
                GL45.GL_MAP_WRITE_BIT | GL45.GL_MAP_PERSISTENT_BIT | GL45.GL_MAP_COHERENT_BIT, null);

        if (mappedMemory == null) throw new RuntimeException("Failed to map GPU Buffer persistently!");
    }

    /** Start writing data for the current frame */
    public ByteBuffer beginWrite() {
        int offset = frameIndex * frameSize;
        mappedMemory.position(offset);
        mappedMemory.limit(offset + frameSize);
        return mappedMemory.slice(); // isolated view for this frame
    }

    /** Finish writing, advance to next frame */
    public void endWrite() {
        frameIndex = (frameIndex + 1) % frameCount;
    }

    /** Get offset of current frame region (for glVertexAttribPointer / glBindBufferRange) */
    public int getCurrentOffset() {
        return frameIndex * frameSize;
    }

    public void clear() {
        this.mappedMemory.clear();
    }

    @Override
    public void delete() {
        GL45.glBindBuffer(GL45.GL_ARRAY_BUFFER, getId());
        GL45.glUnmapBuffer(GL45.GL_ARRAY_BUFFER);
        GL45.glDeleteBuffers(getId());
    }

    @Override
    @Deprecated
    public void upload(ByteBuffer data, int sizeBytes) {
        throw new IllegalStateException("Accessed upload method on MappedGpuBuffer class!");
    }

    @Override
    @Deprecated
    public void update(long offset, ByteBuffer data) {
        throw new IllegalStateException("Accessed update method on MappedGpuBuffer class!");
    }
    
}
