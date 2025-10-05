package pl.epsi.glWrapper.buffers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;
import pl.epsi.glWrapper.buffers.gpu.GpuBuffer;
import pl.epsi.glWrapper.buffers.gpu.MappableGpuBuffer;
import pl.epsi.glWrapper.buffers.gpu.MappableGpuRingBuffer;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.shader.ShaderProgram;
import pl.epsi.glWrapper.shader.ShaderProgramKeys;
import pl.epsi.glWrapper.shader.TextureSamplerUniformProvider;
import pl.epsi.glWrapper.shader.UniformProvider;
import pl.epsi.glWrapper.utils.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class BufferBuilder {

    public final BufferInfo bufferInfo;

    public final ArrayList<AttributeContainer> attributes = new ArrayList<>();
    public final ArrayList<Texture> textures = new ArrayList<>();
    public final ArrayList<UniformProvider> uniformProviders = new ArrayList<>();

    private final int VAO;
    private final MappableGpuRingBuffer mappedVertexBufferObject;
    private final MappableGpuBuffer mappedElementBufferObject;

    @Nullable
    private ShaderProgram shader;

    protected BufferBuilder(BufferInfo bufferInfo, int VAO) {
        this.bufferInfo = bufferInfo;
        attributes.addAll(bufferInfo.vertexFormat.getAttributes());
        this.VAO = VAO;
        this.mappedVertexBufferObject = new MappableGpuRingBuffer(GpuBuffer.BufferTarget.ARRAY_BUFFER, GpuBuffer.BufferUsage.STREAM_DRAW, 3, 1024 * 1000);
        this.mappedElementBufferObject = new MappableGpuBuffer(GpuBuffer.BufferUsage.STREAM_DRAW, GpuBuffer.BufferTarget.ELEMENT_ARRAY_BUFFER, 1024 * 1000);

        enableVertexAttribArrays();
    }

    protected BufferBuilder(DrawMode drawMode, DrawMode.VertexFormat vertexFormat, int VAO) {
        this(new BufferInfo(drawMode, vertexFormat), VAO);
    }

    public BufferBuilder(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(new BufferInfo(drawMode, vertexFormat), GL33.glGenVertexArrays());
    }

    public BufferBuilder(BufferInfo bufferInfo) {
        this(bufferInfo, GL33.glGenVertexArrays());
    }

    // region Builder
    public BufferBuilder vertex(float x, float y, float z) {
        AttributeContainer container = getContainerForType(AttributeType.get("POSITION"));
        container.addValues(x, y, z);
        return this;
    }

    public BufferBuilder color(float a, float r, float g, float b) {
        AttributeContainer container = getContainerForType(AttributeType.get("COLOR"));
        container.addValues(r, g, b, a);
        return this;
    }

    public BufferBuilder texture(float u, float v, Identifier texture) {
        AttributeContainer container = getContainerForType(AttributeType.get("TEXTURE"));
        var textureIndex = Utils.getIndexForObject(this.textures, TexturePool.getTexture(texture));
        if (!this.textures.contains(TexturePool.getTexture(texture))) this.textures.add(TexturePool.getTexture(texture));
        container.addValues(u, v, (float) textureIndex);
        return this;
    }

    public BufferBuilder attrib(AttributeType type, Object... objects) {
        AttributeContainer container = getContainerForType(type);
        return this.attrib(container, objects);
    }

    public BufferBuilder attrib(AttributeContainer container, Object... objects) {
        if (!this.attributes.contains(container)) throw new IllegalArgumentException("Tried to access attribute container with type " + container.getType() + " but this builder does not have it!");
        container.addValues(objects);
        return this;
    }

    public void withShader(@Nullable ShaderProgram shader) {
        this.shader = shader;
    }

    public ShaderProgram withFragmentShader(Identifier fragmentShader) {
        if (fragmentShader == null) {
            this.shader = null;
            return null;
        }
        this.shader = new ShaderProgram(ShaderProgramKeys.getVertexShaderByVertexFormat(this.bufferInfo.vertexFormat), fragmentShader);
        return this.shader;
    }

    public void withVertexAttribute(AttributeContainer container) {
        if (!this.attributes.contains(container)) this.attributes.add(container);
    }

    public void withUniform(UniformProvider up) {
        Lists.add(this.uniformProviders, up);
    }
    // endregion

    public void setupVao() {
        if (this.attributes.size() > this.bufferInfo.vertexFormat.getAttributesSize()) enableVertexAttribArrays();

        GL30.glBindVertexArray(this.VAO);

        this.mappedElementBufferObject.bind();
        this.mappedVertexBufferObject.getCurrentBuffer().bind();

        ByteBuffer mappedMemory = this.mappedVertexBufferObject.beginWrite();
        mappedMemory.order(ByteOrder.nativeOrder());
        long mappedMemoryAddress = MemoryUtil.memAddress(mappedMemory);

        AtomicInteger ptr = new AtomicInteger(0);

        this.attributes.forEach(attribute -> {
            attribute.getBuffer().rewind();
            MemoryUtil.memCopy(MemoryUtil.memAddress(attribute.getBuffer()), mappedMemoryAddress + ptr.get(),
                    (long) attribute.getCount() * attribute.glNumberType.getSizeInBytes());

            if (GlNumberType.shouldUseIPointer(attribute.glNumberType)) {
                GL30.glVertexAttribIPointer(attribute.getLocation(), attribute.getSize(),
                        attribute.getGlNumberType(), 0, ptr.get());
            } else {
                GL30.glVertexAttribPointer(attribute.getLocation(), attribute.getSize(),
                        attribute.getGlNumberType(), attribute.getNormalized(), 0, ptr.get());
            }

            ptr.getAndAdd(attribute.getCount() * attribute.glNumberType.getSizeInBytes());
        });
    }

    public void enableVertexAttribArrays() {
        GL33.glBindVertexArray(this.VAO);
        this.attributes.forEach(attrib -> GL30.glEnableVertexAttribArray(attrib.getLocation()));
        GL33.glBindVertexArray(0);
    }



    public MappableGpuRingBuffer getVBO() { return this.mappedVertexBufferObject; }
    public MappableGpuBuffer getEBO() { return this.mappedElementBufferObject; }

    public int getVAO() {
        // Hacky place for uniforms
        ArrayList<UniformProvider> uniformProviders = new ArrayList<>();

        for (Class<? extends UniformProvider> provider : this.bufferInfo.vertexFormat.getUniformProviders()) {
            if (provider == TextureSamplerUniformProvider.class) {
                uniformProviders.add(new TextureSamplerUniformProvider(this.textures));
            } else {
                try {
                    uniformProviders.add(provider.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create UniformProvider for class " + provider.getName(), e);
                }
            }
        }

        this.uniformProviders.addAll(uniformProviders);

        return this.VAO;
    }

    @Nullable
    public ShaderProgram getShader() { return this.shader; }

    public AttributeContainer getContainerForType(AttributeType type) {
        for (var attrib : attributes) {
            if (attrib.type == type) return attrib;
        }

        throw new IllegalArgumentException("Unknown attribute (" + type + ") for " + this.getClass().getName() + " with VertexFormat " + bufferInfo.vertexFormat);
    }



    public void addToQueue() {
        Renderer.addToRenderQueue(this);
    }

    public void clear() {
        this.attributes.forEach(AttributeContainer::clear);
        this.uniformProviders.clear();
    }



    public record AttributeType(String name) {

        private static final HashMap<String, AttributeType> REGISTRY = new HashMap<>();

        static {
            BufferBuilder.AttributeType.register("POSITION", "COLOR", "TEXTURE");
        }

        public static AttributeType register(String name) {
            return AttributeType.REGISTRY.computeIfAbsent(name, AttributeType::new);
        }

        public static void register(String... names) {
            for (String s : names) {
                AttributeType.REGISTRY.computeIfAbsent(s, AttributeType::new);
            }
        }

        public static AttributeType get(String name) {
            return AttributeType.REGISTRY.get(name);
        }

        public static Collection<AttributeType> values() {
            return Collections.unmodifiableCollection(REGISTRY.values());
        }

        @NotNull
        @Override
        public String toString() {
            return name;
        }

    }

    public static class AttributeContainer {

        private final AttributeType type;
        private final int size;
        private final GlNumberType glNumberType;
        public boolean normalized = false;
        private final int location;

        private final ByteBuffer buffer;

        private int count;

        public AttributeContainer(AttributeType type, int size, GlNumberType glNumberType, int location) {
            this.type = type;
            this.size = size;
            this.glNumberType = glNumberType;
            this.location = location;
            buffer = MemoryUtil.memAlloc(1024 * 1000);
            buffer.order(ByteOrder.nativeOrder());
        }

        public void addValues(Object... values) {
            for (Object obj : values) {
                if (this.glNumberType.getClazz().isInstance(obj)) {
                    this.glNumberType.put(buffer, obj);
                    count++;
                } else {
                    throw new IllegalArgumentException("Provided wrong type for this attribute container! Type: " + obj.getClass().getName());
                }
            }
        }

        public AttributeType getType() { return this.type; }
        public int getGlNumberType() { return this.glNumberType.getGlType(); }
        public int getSize() { return this.size; }
        public boolean getNormalized() { return this.normalized; }
        public int getLocation() { return this.location; }
        public void setNormalized(boolean normalize) { this.normalized = normalize; }

        public int getCount() {
            return this.count;
        }

        public ByteBuffer getBuffer() { return this.buffer; }

        public void clear() {
            buffer.clear();
            count = 0;
        }

        public AttributeContainer copy() {
            return new AttributeContainer(this.type, this.size, this.glNumberType, this.location);
        }

    }

    public static record BufferInfo(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {}

}
