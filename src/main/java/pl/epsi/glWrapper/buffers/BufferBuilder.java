package pl.epsi.glWrapper.buffers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import pl.epsi.glWrapper.buffers.gpu.GpuBuffer;
import pl.epsi.glWrapper.buffers.gpu.MappedGpuBuffer;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.shader.ShaderProgram;
import pl.epsi.glWrapper.shader.ShaderProgramKeys;
import pl.epsi.glWrapper.shader.TextureSamplerUniformProvider;
import pl.epsi.glWrapper.shader.UniformProvider;
import pl.epsi.glWrapper.utils.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.*;

import static pl.epsi.glWrapper.buffers.gpu.GpuBuffer.BufferTarget;
import static pl.epsi.glWrapper.buffers.gpu.GpuBuffer.BufferUsage;

public class BufferBuilder {

    public final DrawMode drawMode;
    public final DrawMode.VertexFormat vertexFormat;

    public final ArrayList<AttributeContainer> attributes = new ArrayList<>();
    public final ArrayList<Texture> textures = new ArrayList<>();

    public ArrayList<UniformProvider> uniformProviders = new ArrayList<>();

    private final int VAO;
    private final MappedGpuBuffer VBO;
    private final GpuBuffer EBO;
    private final Identifier id;
    private ArrayList<Integer> indices = new ArrayList<>();

    @Nullable
    private ShaderProgram shader;

    protected BufferBuilder(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat, int VAO, MappedGpuBuffer VBO, GpuBuffer EBO) {
        this.id = id;
        this.drawMode = drawMode;
        this.vertexFormat = vertexFormat;
        attributes.addAll(vertexFormat.getAttributes());
        this.VAO = VAO;
        this.VBO = VBO;
        this.EBO = EBO;

        this.setupAttribPointers();
    }

    /// Allocating around 1MB of data for vertices
    protected BufferBuilder(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(new Identifier("DefaultBuilder#" + drawMode + "#" + vertexFormat), drawMode, vertexFormat, GL33.glGenVertexArrays(),
                new MappedGpuBuffer(BufferTarget.ARRAY_BUFFER, BufferUsage.DYNAMIC_DRAW, 1, 1000 * 1024),
                new GpuBuffer(BufferTarget.ELEMENT_ARRAY_BUFFER, BufferUsage.DYNAMIC_DRAW));
    }

    /// Allocating around 1MB of data for vertices
    public BufferBuilder(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(id, drawMode, vertexFormat, GL33.glGenVertexArrays(),
                new MappedGpuBuffer(BufferTarget.ARRAY_BUFFER, BufferUsage.DYNAMIC_DRAW, 1, 1000 * 1024),
                new GpuBuffer(BufferTarget.ELEMENT_ARRAY_BUFFER, BufferUsage.DYNAMIC_DRAW));
    }

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
        return this.texture(u, v, TexturePool.getTexture(texture));
    }

    public BufferBuilder texture(float u, float v, Texture texture) {
        AttributeContainer container = getContainerForType(AttributeType.get("TEXTURE"));
        if (!this.textures.contains(texture)) this.textures.add(texture);
        var textureIndex = Utils.getIndexForObject(this.textures, texture);
        container.addValues(u, v, (float) textureIndex);
        return this;
    }

    public BufferBuilder normal(float nx, float ny, float nz) {
        AttributeContainer container = getContainerForType(AttributeType.get("NORMAL"));
        container.addValues(nx, ny, nz);
        return this;
    }

    public BufferBuilder index(int index) {
        if (index < 0) {
            System.out.println("Less that 0!");
        }
        AttributeContainer container = getContainerForType(AttributeType.get("MODEL_INDEX"));
        container.addValues(index);
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

    public void setupAttribPointers() {
        GL30.glBindVertexArray(this.VAO);
        VBO.bind();

        int stride = 0;
        int pointer = 0;
        int frameOffset = VBO.getCurrentOffset();

        for (var attribute : this.attributes) {
            stride += attribute.getCount() * attribute.glNumberType.getSizeInBytes();
        }

        for (var attribute : this.attributes) {
            if (GlNumberType.shouldUseIPointer(attribute.glNumberType)) {
                GL30.glVertexAttribIPointer(attribute.getLocation(), attribute.getSize(),
                        attribute.getGlNumberType(), stride, pointer);
            } else {
                GL30.glVertexAttribPointer(attribute.getLocation(), attribute.getSize(),
                        attribute.getGlNumberType(), attribute.getNormalized(), stride, pointer);
            }

            GL30.glEnableVertexAttribArray(attribute.getLocation());
            pointer += attribute.getCount() * attribute.glNumberType.getSizeInBytes();
        }
    }

    private int ia = 0;

    public void setupVao() {
        //if (ia <= 3) {
        Profiler.startSection("Data Setup");
        GL30.glBindVertexArray(this.VAO);
        EBO.bind();
        VBO.bind();

        //int positionAttribCount = getContainerForType(AttributeType.get("POSITION")).getAttributeAmount();
        ByteBuffer vboData = VBO.beginWrite();
        vboData.order(ByteOrder.nativeOrder());

        //for (int i = 0; i < positionAttribCount; i++) {
        //    for (AttributeContainer attribContainer : this.attributes) {
        //        for (int j = 0; j < attribContainer.getSize(); j++) {
        //            attribContainer.glNumberType.put(vboData, attribContainer.getObjects().get(i * attribContainer.getSize() + j));
        //        }
        //    }
        //}

        //for (var container : this.attributes) {
        //    container.getObjects().forEach(obj -> {
        //        container.glNumberType.put(vboData, obj);
        //    });
        //}
        int a = this.getContainerForType(AttributeType.get("POSITION")).getAttributeAmount();
        for (int i = 0; i < a; i++) {
            vboData.putFloat(1);
            vboData.putFloat(1);
            vboData.putFloat(1);
        }

        for (int i = 0; i < getContainerForType(AttributeType.get("TEXTURE")).getAttributeAmount(); i++) {
            vboData.putFloat(1);
            vboData.putFloat(1);
            vboData.putFloat(0);
        }

        for (int i = 0; i < getContainerForType(AttributeType.get("NORMAL")).getAttributeAmount(); i++) {
            vboData.putFloat(1);
            vboData.putFloat(1);
            vboData.putFloat(1);
        }

        for (int i = 0; i < getContainerForType(AttributeType.get("MODEL_INDEX")).getAttributeAmount(); i++) {
            vboData.putInt(0);
        }

        VBO.endWrite();
        Profiler.endSection();
    }

    public int getVAO() {
        // Hacky place for uniforms
        if (ia == 1) {
            ArrayList<UniformProvider> uniformProviders = new ArrayList<>();

            for (Class<? extends UniformProvider> provider : this.vertexFormat.getUniformProviders()) {
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
        }

        return this.VAO;
    }

    public GpuBuffer getEBO() { return this.EBO; }
    public MappedGpuBuffer getVBO() { return this.VBO; }

    public void clear() {
        this.attributes.forEach(AttributeContainer::clear);
        this.uniformProviders.clear();
        this.indices.clear();
        this.textures.clear();
    }

    public void withVertexAttribute(AttributeContainer container) {
        if (!this.attributes.contains(container)) this.attributes.add(container);
        this.setupAttribPointers();
    }

    public void withUniform(UniformProvider up) {
        Lists.add(this.uniformProviders, up);
    }


    public void withShader(@Nullable ShaderProgram shader) {
        this.shader = shader;
    }

    public ShaderProgram withFragmentShader(Identifier fragmentShader) {
        if (fragmentShader == null) {
            this.shader = null;
            return null;
        }
        this.shader = new ShaderProgram(ShaderProgramKeys.getVertexShaderByVertexFormat(this.vertexFormat), fragmentShader);
        return this.shader;
    }

    public void addToQueue() {
        Renderer.addToRenderQueue(this);
    }

    public void setIndices(ArrayList<Integer> indices) {
        this.indices = indices;
    }

    public ArrayList<Integer> getIndices() { return this.indices; }

    public Identifier getID() { return this.id; }

    @Nullable
    public ShaderProgram getShader() { return this.shader; }

    public AttributeContainer getContainerForType(AttributeType type) {
        for (var attrib : attributes) {
            if (attrib.type == type) return attrib;
        }

        throw new IllegalArgumentException("Unknown attribute (" + type + ") for " + this.getClass().getName() + " with VertexFormat " + vertexFormat);
    }

    public record AttributeType(String name) {

        private static final HashMap<String, AttributeType> REGISTRY = new HashMap<>();

        static {
            BufferBuilder.AttributeType.register("POSITION", "COLOR", "TEXTURE", "NORMAL", "MODEL_INDEX");
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

        private final ArrayList<Object> objects = new ArrayList<>();

        public AttributeContainer(AttributeType type, int size, GlNumberType glNumberType, int location) {
            this.type = type;
            this.size = size;
            this.glNumberType = glNumberType;
            this.location = location;
        }

        public void addValues(Object... values) {
            for (Object obj : values) {
                if (this.glNumberType.getClazz().isInstance(obj)) {
                    objects.add(obj);
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
            return objects.size();
        }

        public int getAttributeAmount() {
            return objects.size() / this.size;
        }

        public ArrayList<Object> getObjects() { return this.objects; }

        public void clear() {
            objects.clear();
        }

        public AttributeContainer copy() {
            return new AttributeContainer(this.type, this.size, this.glNumberType, this.location);
        }

    }

}
