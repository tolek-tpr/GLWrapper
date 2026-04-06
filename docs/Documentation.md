# GLWrapper Documentation

## Table of Contents
1. [Introduction](#introduction)
2. [Core Concepts](#core-concepts)
3. [Getting Started](#getting-started)
4. [Rendering with BufferBuilder](#rendering-with-bufferbuilder)
5. [Immediate Rendering](#immediate-rendering)
6. [The DrawContext](#the-drawcontext)
7. [Resource Identifiers](#resource-identifiers)
8. [Advanced Usage](#advanced-usage)
   - [Custom Fragment Shader](#custom-fragment-shader)
   - [Custom Shader Program](#custom-shader-program)
   - [Custom Vertex Attributes](#custom-vertex-attributes)
   - [Custom Uniforms](#custom-uniforms)

---

## Introduction

GLWrapper is a Java rendering library built on top of [LWJGL](https://www.lwjgl.org/) that abstracts away the low-level mechanics of OpenGL. It is designed for developers who want to render 2D graphics without having to manage vertex buffer objects, shader compilation, vertex attribute pointers, or index buffers manually.

With GLWrapper, the typical rendering workflow is reduced to describing what you want to draw — positions, colors, textures — and calling a single method to submit it for rendering. The library handles the rest.

> **Note:** Some OpenGL knowledge is required for advanced features such as writing custom shaders. The core rendering API, however, requires none.

---

## Core Concepts

### The Rendering Model

GLWrapper operates on a **queue-and-flush** model. During each frame, you add one or more `BufferBuilder` objects to a render queue. At the end of the frame, a single call to `Renderer.render()` flushes the queue — drawing everything that was queued and clearing it for the next frame.

This means the order in which you call drawing methods within a frame determines the order in which things are drawn, and every frame starts from a clean slate.

### Coordinate System

GLWrapper uses **screen-space pixel coordinates** by default. The origin `(0, 0)` is at the **bottom-left** of the window, with x increasing to the right and y increasing upward. Coordinates match pixel positions directly, so a vertex at `(400, 300, 0)` on an 800×600 window is in the center of the screen.

The `z` coordinate controls depth ordering. Objects with a higher `z` value are drawn in front of objects with a lower `z` value. For most 2D use cases, `z = 0` is sufficient.

This coordinate system is provided automatically by the built-in orthographic projection matrix. To activate it, call `Renderer.updateProjMatrix(width, height)` with your window dimensions, typically inside a window resize callback.

### Vertex Formats

A **vertex format** describes what data each vertex carries. GLWrapper provides four built-in formats as the `DrawMode.VertexFormat` enum:

| Format | Data per vertex |
|---|---|
| `POSITION` | x, y, z coordinates only |
| `POSITION_COLOR` | x, y, z coordinates + ARGB color |
| `POSITION_TEXTURE` | x, y, z coordinates + UV texture coordinates |
| `POSITION_COLOR_TEXTURE` | x, y, z coordinates + ARGB color + UV texture coordinates |

You select a vertex format when obtaining a `BufferBuilder`. GLWrapper automatically uses the correct built-in shader for each format.

### Draw Modes

A **draw mode** determines how OpenGL interprets the sequence of vertices you provide. GLWrapper exposes the following modes via the `DrawMode` enum:

| Mode | Description |
|---|---|
| `LINES` | Every two vertices form an independent line segment |
| `LINE_STRIP` | Vertices are connected into a continuous line |
| `TRIANGLES` | Every three vertices form an independent triangle |
| `TRIANGLE_STRIP` | Vertices form a strip of connected triangles |
| `TRIANGLE_FAN` | All triangles share a common first vertex |

For drawing solid shapes, `TRIANGLES` and `TRIANGLE_STRIP` are the most common choices.

### BufferBuilder vs Immediate

GLWrapper provides two rendering modes:

- **`BufferBuilder`** — A queued buffer. Vertices are accumulated and submitted to the render queue when `addToQueue()` is called. The actual drawing happens later, when `Renderer.render()` is invoked.
- **`Immediate`** — An immediate buffer. Drawing happens the moment `end()` is called, bypassing the queue. Use this when you need precise control over draw order relative to other queued content.

---

## Getting Started

### Prerequisites

- A working LWJGL project with a GLFW window and an active OpenGL context.
- GLWrapper added as a dependency. See [Setup.md](Setup.md) for instructions.

### Initializing GLWrapper

After creating your OpenGL context, call `Renderer.updateProjMatrix(width, height)` to set up the orthographic projection. This should also be called whenever the window is resized.

Then, at the end of every frame in your render loop, call `Renderer.render()` to flush the queue and draw everything that was submitted.

```java
public class App {

    long window;

    public void run() {
        // ... GLFW and OpenGL context initialization ...

        // Set the projection matrix once after context creation
        Renderer.updateProjMatrix(800, 600);

        loop();
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // --- your drawing code goes here ---

            Renderer.render(); // flush the queue and draw everything

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

}
```

---

## Rendering with BufferBuilder

### Obtaining a BufferBuilder

Use `Buffers.getBuffer(DrawMode, VertexFormat)` to retrieve a managed `BufferBuilder` for a given draw mode and vertex format combination. GLWrapper maintains a pool of these internally, so calling `getBuffer` with the same arguments always returns the same reusable instance.

```java
BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
```

### Adding Vertices

`BufferBuilder` exposes a fluent API for adding vertex data. Each vertex begins with a `.vertex(x, y, z)` call, followed by the additional data required by the chosen vertex format.

**Position only (`POSITION`):**
```java
builder.vertex(100, 100, 0);
```

**Position and color (`POSITION_COLOR`):**

Colors are specified as four floats in `(alpha, red, green, blue)` order, each in the range `0.0` to `1.0`.
```java
builder.vertex(100, 100, 0).color(1f, 1f, 0f, 0f); // fully opaque red
```

**Position and texture (`POSITION_TEXTURE`):**

UV coordinates `(u, v)` define where on the texture this vertex maps. The texture is identified by an `Identifier` (see [Resource Identifiers](#resource-identifiers)).
```java
builder.vertex(100, 100, 0).texture(0f, 0f, new Identifier("myapp", "textures/sprite.png"));
```

**Position, color, and texture (`POSITION_COLOR_TEXTURE`):**
```java
builder.vertex(100, 100, 0)
       .color(1f, 1f, 1f, 1f)
       .texture(0f, 0f, new Identifier("myapp", "textures/sprite.png"));
```

### Submitting for Rendering

Once all vertices are added, call `addToQueue()` to register the buffer with the renderer. This only needs to be called once per frame per builder. Subsequent calls within the same frame are ignored.

```java
builder.addToQueue();
```

### Complete Example

```java
while (!glfwWindowShouldClose(window)) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
    builder.vertex(0,   0,   0).color(1f, 1f, 0f, 0f);
    builder.vertex(200, 0,   0).color(1f, 0f, 1f, 0f);
    builder.vertex(100, 200, 0).color(1f, 0f, 0f, 1f);
    builder.addToQueue();

    Renderer.render();

    glfwSwapBuffers(window);
    glfwPollEvents();
}
```

---

## Immediate Rendering

An `Immediate` buffer draws its contents to the screen the moment `end()` is called, before the render queue is flushed. This is useful when you need to guarantee that something is drawn before the queued content.

Obtain an `Immediate` from `Buffers.getImmediate(DrawMode, VertexFormat)`:

```java
Immediate immediate = Buffers.getImmediate(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION);
```

Add vertices the same way as a `BufferBuilder`, then call `end()` to draw immediately:

```java
immediate.vertex(50, 50, 0);
immediate.vertex(150, 50, 0);
immediate.vertex(100, 150, 0);
immediate.end(); // drawn right now, before Renderer.render() is called
```

### Ordering Example

In the following example, even though `builder.addToQueue()` is called before `immediate.end()`, the `Immediate` content will appear behind the queued triangle, because `Immediate` draws at call time while queued content draws later at `Renderer.render()`.

```java
BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.vertex(0, 0, 0).color(1f, 1f, 0f, 0f);
builder.vertex(200, 0, 0).color(1f, 0f, 1f, 0f);
builder.vertex(100, 200, 0).color(1f, 0f, 0f, 1f);
builder.addToQueue(); // queued — drawn at Renderer.render()

Immediate immediate = Buffers.getImmediate(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION);
immediate.vertex(0, 0, 0);
immediate.vertex(300, 0, 0);
immediate.vertex(150, 300, 0);
immediate.end(); // drawn immediately — appears behind the queued triangle

Renderer.render(); // queued content drawn here — appears on top
```

---

## The DrawContext

`DrawContext` provides static utility methods for drawing common shapes without manually specifying vertices. Each method has two variants: one that automatically retrieves the appropriate `BufferBuilder` from the pool, and one that accepts an explicit `BufferBuilder` (or `Immediate`) for use with custom shaders.

All `DrawContext` methods accept a `boolean queue` parameter. When `true`, `addToQueue()` is called automatically. When `false`, you can continue adding other shapes to the same buffer before queuing it yourself.

Colors in `DrawContext` are specified as packed ARGB integers (e.g., `0xFF_FF0000` for fully opaque red).

### drawRect

Draws a filled rectangle defined by two corner coordinates.

```java
// Signature
DrawContext.drawRect(int x1, int y1, int x2, int y2, int z, boolean queue);
DrawContext.drawRect(int x1, int y1, int x2, int y2, int z, int color, boolean queue);
DrawContext.drawRect(BufferBuilder builder, int x1, int y1, int x2, int y2, int z, boolean queue);
DrawContext.drawRect(BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int color, boolean queue);
```

- `(x1, y1)` — one corner of the rectangle in screen-space pixels.
- `(x2, y2)` — the opposite corner.
- `z` — depth.
- `color` — packed ARGB color. Defaults to `0xFFFFFFFF` (fully opaque white) if omitted.
- `queue` — whether to call `addToQueue()` automatically.

```java
// Draw a red rectangle from (50, 50) to (250, 150)
DrawContext.drawRect(50, 50, 250, 150, 0, 0xFF_FF0000, true);
```

### drawGradient

Draws a filled rectangle with a linear color gradient between two colors along a specified axis.

```java
// Signature
DrawContext.drawGradient(int x1, int y1, int x2, int y2, int z,
                         GradientDirection direction, int startColor, int endColor, boolean queue);
DrawContext.drawGradient(BufferBuilder builder, int x1, int y1, int x2, int y2, int z,
                         GradientDirection direction, int startColor, int endColor, boolean queue);
```

- `direction` — a `GradientDirection` enum value controlling the gradient axis:
  - `TOP_TO_BOTTOM` (default when `null` is passed)
  - `BOTTOM_TO_TOP`
  - `LEFT_TO_RIGHT`
  - `RIGHT_TO_LEFT`
- `startColor` / `endColor` — packed ARGB colors at the start and end of the gradient respectively.

```java
// Draw a gradient rectangle fading from blue at the top to transparent at the bottom
DrawContext.drawGradient(
    100, 100, 400, 400, 0,
    GradientDirection.TOP_TO_BOTTOM,
    0xFF_0000FF, // opaque blue
    0x00_0000FF, // transparent blue
    true
);
```

### drawTexture

Draws a textured rectangle. The texture is identified by an `Identifier` pointing to an image file in the resource folder.

GLWrapper provides multiple overloads with increasing levels of control:

```java
// Draw a texture filling the specified region, using full UV coverage
DrawContext.drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z, boolean queue);

// Draw a texture with a specific UV offset (u, v)
DrawContext.drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z,
                        float u, float v, boolean queue);

// Draw a sub-region of a texture, specifying UV offset and the texture's pixel dimensions
DrawContext.drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z,
                        float u, float v, int texWidth, int texHeight, boolean queue);

// As above, with a tint color
DrawContext.drawTexture(Identifier texture, int x1, int y1, int x2, int y2, int z,
                        float u, float v, int texWidth, int texHeight, int color, boolean queue);
```

Each overload also has a variant that accepts an explicit `BufferBuilder` as the first argument.

```java
// Draw a sprite.png texture filling the region (0, 0) to (128, 128)
DrawContext.drawTexture(
    new Identifier("myapp", "textures/sprite.png"),
    0, 0, 128, 128, 0,
    true
);
```

---

## Resource Identifiers

GLWrapper uses `Identifier` objects to reference files in the classpath — most commonly shaders and textures. An `Identifier` is constructed from a **namespace** and a **path**:

```java
new Identifier("myapp", "textures/sprite.png")
```

This resolves to the classpath resource at:
```
resources/myapp/assets/textures/sprite.png
```

The namespace maps to a top-level folder in `resources/`. The `/assets/` segment is added automatically. The path is relative to that `assets` folder.

For the built-in GLWrapper resources, the namespace is `glw`:
```java
new Identifier("glw", "shaders/core/position_color_program.fsh")
// resolves to: resources/glw/assets/shaders/core/position_color_program.fsh
```

---

## Advanced Usage

### Custom Fragment Shader

To apply a custom look to a shape while keeping the built-in vertex processing, provide a custom fragment shader via `withFragmentShader`. GLWrapper will pair it with the standard vertex shader for the chosen `VertexFormat`.

```java
BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.withFragmentShader(new Identifier("myapp", "shaders/my_effect.fsh"));

builder.vertex(0, 0, 0).color(1f, 1f, 1f, 1f);
builder.vertex(200, 0, 0).color(1f, 1f, 1f, 1f);
builder.vertex(100, 200, 0).color(1f, 1f, 1f, 1f);
builder.addToQueue();

Renderer.render();
```

You can also pass a custom-shader builder directly to `DrawContext`:

```java
BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.withFragmentShader(new Identifier("myapp", "shaders/my_effect.fsh"));

DrawContext.drawRect(builder, 0, 0, 300, 300, 0, true);

Renderer.render();
```

To reset a builder back to its default shader, call `withShader(null)`:

```java
builder.withShader(null);
```

> **Note:** Modifying the default pooled builders from `Buffers.getBuffer()` with a custom shader is possible but not recommended, as the modified shader will persist until explicitly reset.

### Custom Shader Program

For full control over both the vertex and fragment stages, construct a `ShaderProgram` explicitly and assign it with `withShader`:

```java
BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.withShader(new ShaderProgram(
    new Identifier("myapp", "shaders/my_shader.vsh"),
    new Identifier("myapp", "shaders/my_shader.fsh")
));

DrawContext.drawRect(builder, 0, 0, 300, 300, 0, true);

Renderer.render();
```

### Custom Vertex Attributes

You can add additional per-vertex data beyond what the built-in formats provide. This requires a custom shader that reads the attribute, an `AttributeType` key, and an `AttributeContainer` that describes the data layout.

**Step 1 — Register an AttributeType:**

```java
BufferBuilder.AttributeType myType = BufferBuilder.AttributeType.register("MY_ATTRIBUTE");
```

**Step 2 — Create an AttributeContainer:**

```java
// AttributeContainer(type, size, GlNumberType, shaderLocation)
BufferBuilder.AttributeContainer myContainer =
    new BufferBuilder.AttributeContainer(myType, 1, GlNumberType.FLOAT, 3);
```

- `size` — number of values of this type per vertex (e.g., `1` for a single float, `3` for a vec3).
- `GlNumberType` — the data type: `FLOAT`, `INT`, `BYTE`, `SHORT`, or `DOUBLE`.
- `shaderLocation` — the `layout(location = X)` index in your vertex shader.

The corresponding GLSL declaration for the example above:
```glsl
layout(location = 3) in float aMyAttribute;
```

If you need the values normalized to `[0.0, 1.0]`, call:
```java
myContainer.setNormalized(true);
```

**Step 3 — Register the container and write data:**

```java
BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.withShader(new ShaderProgram(
    new Identifier("myapp", "shaders/custom.vsh"),
    new Identifier("myapp", "shaders/custom.fsh")
));
builder.withVertexAttribute(myContainer);

builder.vertex(0,   0,   0).color(1f, 1f, 1f, 1f).attrib(myType, 0f);
builder.vertex(200, 0,   0).color(1f, 1f, 1f, 1f).attrib(myType, 0f);
builder.vertex(100, 200, 0).color(1f, 1f, 1f, 1f).attrib(myType, 1f);
builder.addToQueue();

Renderer.render();
```

### Custom Uniforms

Uniforms allow you to pass arbitrary data to a shader each frame. Implement the `UniformProvider` interface and register it on the builder with `withUniform`.

```java
UniformProvider timeProvider = new UniformProvider() {
    @Override
    public void apply(ShaderProgram program) {
        program.uniformFloat("uTime", (float) glfwGetTime());
    }
};

builder.withUniform(timeProvider);
```

For reusable uniform providers, implementing them as records is a clean pattern:

```java
public record ViewMatrixUniformProvider(Matrix4f viewMatrix) implements UniformProvider {
    @Override
    public void apply(ShaderProgram program) {
        program.uniformMat4f("uViewMatrix", viewMatrix);
    }
}
```

Then register it on any builder:

```java
builder.withUniform(new ViewMatrixUniformProvider(myViewMatrix));
```

**Available `ShaderProgram` uniform methods:**

| Method | GLSL type |
|---|---|
| `uniformFloat(String name, float value)` | `float` / `uniform float` |
| `uniformMat4f(String name, Matrix4f matrix)` | `mat4` |
| `uniformTexture(String name, int slot)` | `sampler2D` |
| `uniformIntArray(String name, int[] array)` | `int[]` |
