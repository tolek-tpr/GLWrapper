package pl.epsi.glWrapper.buffers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import pl.epsi.glWrapper.render.Renderer;
import pl.epsi.glWrapper.shader.ShaderProgram;
import pl.epsi.glWrapper.shader.ShaderProgramKeys;
import pl.epsi.glWrapper.shader.TextureSamplerUniformProvider;
import pl.epsi.glWrapper.shader.UniformProvider;
import pl.epsi.glWrapper.utils.*;

import java.util.*;
import java.util.stream.Collectors;

public class BufferBuilder {

    public static final int EBO = GL33.glGenBuffers();
    public static final int MAX_BUFFER_SIZE = 3000;

    public final DrawMode drawMode;
    public final DrawMode.VertexFormat vertexFormat;

    public final ArrayList<AttributeContainer> attributes = new ArrayList<>();
    public final ArrayList<Texture> textures = new ArrayList<>();

    public ArrayList<UniformProvider> uniformProviders = new ArrayList<>();

    private final int VAO;
    private final Identifier id;
    private final HashMap<AttributeType, Integer> VBOs;
    private ArrayList<Integer> indices = new ArrayList<>();

    @Nullable
    private ShaderProgram shader;

    protected BufferBuilder(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat, int VAO) {
        this.id = id;
        this.drawMode = drawMode;
        this.vertexFormat = vertexFormat;
        attributes.addAll(vertexFormat.getAttributes());
        this.VBOs = VertexBufferHandler.getVBOsForVertexFormat(this.vertexFormat);
        this.VAO = VAO;
    }

    protected BufferBuilder(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(new Identifier("DefaultBuilder#" + drawMode + "#" + vertexFormat), drawMode, vertexFormat, GL33.glGenVertexArrays());
    }

    public BufferBuilder(Identifier id, DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(id, drawMode, vertexFormat, GL33.glGenVertexArrays());
    }

    public BufferBuilder vertex(float x, float y, float z) {
        AttributeContainer container = getContainerForType(AttributeType.get("POSITION"));
        checkContainerSpace(container, 1);
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

    public void setupVao() {
        GL30.glBindVertexArray(this.VAO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, BufferBuilder.EBO);
        this.attributes.forEach((attribute) -> {
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.getVBO(attribute.getType()));

            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, (long) attribute.getCount() * Float.BYTES, GL30.GL_DYNAMIC_DRAW);

            if (GlNumberType.shouldUseIPointer(attribute.glNumberType)) {
                GL30.glVertexAttribIPointer(attribute.getLocation(), attribute.getSize(),
                        attribute.getGlNumberType(), 0, 0);
            } else {
                GL30.glVertexAttribPointer(attribute.getLocation(), attribute.getSize(),
                        attribute.getGlNumberType(), attribute.getNormalized(), 0, 0);
            }

            GL30.glEnableVertexAttribArray(attribute.getLocation());

        });

        // Reset
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0);
    }

    public int getVAO() {
        // Hacky place for uniforms
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

        return this.VAO;
    }

    public void clear() {
        this.attributes.forEach(AttributeContainer::clear);
        this.uniformProviders.clear();
        this.indices.clear();
        this.textures.clear();
    }

    public void checkContainerSpace(AttributeContainer container, int incomingVertexCount) {
        int attributeCount = container.getAttributeAmount();
        int attributeAmount = container.getCount();
        boolean canFill = attributeCount + incomingVertexCount <= BufferBuilder.MAX_BUFFER_SIZE;
        if (canFill) return;

        BufferBuilder builder = this instanceof BufferBuilder3D ?
                new BufferBuilder3D(this.drawMode, this.vertexFormat) : new BufferBuilder(this.drawMode, this.vertexFormat);
        builder.copyFrom(this);

        this.clear();

        if (this.drawMode == DrawMode.LINES) {
            if (attributeCount % 2 == 0) return;
            builder.attributes.forEach(aContainer -> {
                int size = aContainer.getSize();
                int count = aContainer.getCount();
                for (int i = size; i > 0; i--) {
                    this.attrib(aContainer.getType(), aContainer.getObjects().get(count - i));
                }
            });
        } else if (this.drawMode == DrawMode.LINE_STRIP) {
            builder.attributes.forEach(aContainer -> {
                int count = aContainer.getCount();
                int size = aContainer.getSize();
                for (int i = size; i > 0; i--) {
                    this.attrib(aContainer.getType(), aContainer.getObjects().get(count - i));
                }
            });
        } else if (this.drawMode == DrawMode.TRIANGLES) {
            int mod = attributeAmount % 3;
            if (mod != 0) {
                builder.attributes.forEach(aContainer -> {
                    int size = aContainer.getSize() * mod;
                    int count = aContainer.getCount();
                    for (int i = size; i > 0; i--) {
                        this.attrib(aContainer.getType(), aContainer.getObjects().get(count - i));
                    }
                });
            }
        } else if (this.drawMode == DrawMode.TRIANGLE_STRIP) {
            builder.attributes.forEach(aContainer -> {
                int count = aContainer.getCount();
                int size = aContainer.getSize() * 2;
                for (int i = size; i > 0; i--) {
                    this.attrib(aContainer.getType(), aContainer.getObjects().get(count - i));
                }
            });
        } else if (this.drawMode == DrawMode.TRIANGLE_FAN) {
            builder.attributes.forEach(aContainer -> {
                int size = aContainer.getSize();
                for (int i = 0; i < size; i++) {
                    this.attrib(aContainer.getType(), aContainer.getObjects().get(i));
                }
            });
        }

        if (RenderSystem.shouldRenderFullBuffersImmediately()) {
            Renderer.renderBuffer(builder);
        } else {
            builder.addToQueue();
        }
    }

    public void withVertexAttribute(AttributeContainer container) {
        if (!this.attributes.contains(container)) this.attributes.add(container);
        if (!this.VBOs.containsKey(container.getType())) this.VBOs.put(container.getType(), VertexBufferHandler.getVBO(container.getType()));
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

    public Integer getVBO(AttributeType type) {
        if (!VBOs.containsKey(type)) throw new IllegalArgumentException("Illegal attribute type " + type + " for vertex format " + vertexFormat);
        return VBOs.get(type);
    }

    public AttributeContainer getContainerForType(AttributeType type) {
        for (var attrib : attributes) {
            if (attrib.type == type) return attrib;
        }

        throw new IllegalArgumentException("Unknown attribute (" + type + ") for " + this.getClass().getName() + " with VertexFormat " + vertexFormat);
    }

    public void copyFrom(BufferBuilder builder) {
        this.clear();
        this.attributes.clear();
        this.attributes.addAll(builder.attributes.stream().map(AttributeContainer::copyWithData).collect(Collectors.toCollection(ArrayList::new)));
        this.uniformProviders.addAll(builder.uniformProviders);
        this.indices.addAll(builder.indices);
        this.VBOs.putAll(builder.VBOs);
        this.textures.addAll(builder.textures);
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

        private ArrayList<Object> objects = new ArrayList<>();

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

        public AttributeContainer copyWithData() {
            var container = new AttributeContainer(this.type, this.size, this.glNumberType, this.location);
            container.normalized = this.normalized;
            container.objects.addAll(this.objects);
            return container;
        }

    }

}
