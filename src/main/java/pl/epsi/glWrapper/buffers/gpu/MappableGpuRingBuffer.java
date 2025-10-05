package pl.epsi.glWrapper.buffers.gpu;

import java.nio.ByteBuffer;

public class MappableGpuRingBuffer {

    private final MappableGpuBuffer[] buffers;
    private final int frameCount;

    private int frameIndex = 0;

    public MappableGpuRingBuffer(GpuBuffer.BufferTarget target, GpuBuffer.BufferUsage usage, int frameCount, int frameSize) {
        this.frameCount = frameCount;
        this.buffers = new MappableGpuBuffer[frameCount];

        for (int i = 0; i < frameCount; i++) {
            this.buffers[i] = new MappableGpuBuffer(usage, target, frameSize);
        }
    }

    public ByteBuffer beginWrite() {
        return buffers[frameIndex].getBuffer();
    }

    public void rotate() {
        this.frameIndex = (this.frameIndex + 1) % this.frameCount;
    }

    public MappableGpuBuffer getCurrentBuffer() { return this.buffers[frameIndex]; }

    public void clear() {
        for (MappableGpuBuffer buffer : buffers) {
            buffer.clear();
        }
    }

    public void delete() {
        for (MappableGpuBuffer buffer : buffers) {
            buffer.delete();
        }
    }

}
