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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class BufferBuilder {

    public static final int EBO = GL33.glGenBuffers();
    public static final int MAX_BUFFER_SIZE = 1000;

    public final DrawMode drawMode;
    public final DrawMode.VertexFormat vertexFormat;

    public final ArrayList<AttributeContainer> attributes = new ArrayList<>();

    private final HashMap<AttributeType, Integer> VBOs;

    public final ArrayList<Texture> textures = new ArrayList<>();

    public ArrayList<UniformProvider> uniformProviders = new ArrayList<>();

    private final int VAO;

    @Nullable
    private ShaderProgram shader;

    protected BufferBuilder(DrawMode drawMode, DrawMode.VertexFormat vertexFormat, int VAO) {
        this.drawMode = drawMode;
        this.vertexFormat = vertexFormat;
        attributes.addAll(vertexFormat.getAttributes());
        this.VBOs = VertexBufferHandler.getVBOsForVertexFormat(this.vertexFormat);
        this.VAO = VAO;
    }

    public BufferBuilder(DrawMode drawMode, DrawMode.VertexFormat vertexFormat) {
        this(drawMode, vertexFormat, GL33.glGenVertexArrays());
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

    public void setupVao() {
        GL30.glBindVertexArray(this.VAO);
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, BufferBuilder.EBO);
        this.attributes.forEach((attribute) -> {
            GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, this.getVBO(attribute.getType()));
            GL30.glBufferData(GL30.GL_ARRAY_BUFFER, (long) MAX_BUFFER_SIZE * Float.BYTES * attribute.getSize(), GL30.GL_DYNAMIC_DRAW);

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

    public void addToQueue() {
        Renderer.addToRenderQueue(this);
    }

    public void clear() {
        this.attributes.forEach(AttributeContainer::clear);
        this.uniformProviders.clear();
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

    public void withVertexAttribute(AttributeContainer container) {
        if (!this.attributes.contains(container)) this.attributes.add(container);
        if (!this.VBOs.containsKey(container.getType())) this.VBOs.put(container.getType(), VertexBufferHandler.getVBO(container.getType()));
    }

    public void withUniform(UniformProvider up) {
        Lists.add(this.uniformProviders, up);
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

        // Should only be used for position
        public int getCount() {
            if (this.type != AttributeType.get("POSITION")) throw new IllegalStateException("Executed getCount on AttributeContainer that is a " + type + " type");
            return objects.size();
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
