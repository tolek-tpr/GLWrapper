# Improvement Plan

---

## Must-Haves

These are issues that affect correctness, stability, or make the library unsuitable for real-world use.

### 1. Memory Leak in AttributeContainer

Every `AttributeContainer` allocates **512 MB** of off-heap memory via `MemoryUtil.memAlloc`. This memory is never freed — there is no `MemoryUtil.memFree` call anywhere in the codebase. In a running application this will silently consume all available native memory over time.

The fix involves two parts: right-sizing the allocation (512 MB per attribute per buffer is far too large for nearly any use case), and providing a `dispose()` method on `BufferBuilder` that frees all associated off-heap memory and deletes the GPU objects (VAO, VBO, EBO).

### 2. No Alpha Blending Enabled by Default

OpenGL does not enable alpha blending out of the box. Drawing anything with a non-opaque alpha value currently produces incorrect results unless the user manually calls `glEnable(GL_BLEND)` and configures `glBlendFunc`. This is a stumbling block that will catch most users off guard. GLWrapper should enable blending with a sensible default (`SRC_ALPHA, ONE_MINUS_SRC_ALPHA`) during initialization.

### 3. Silent Texture Failures

In `Texture.java`, failed texture loads are handled with `assert` statements. The JVM disables assertions by default, meaning a missing or corrupt texture file produces no error at all — the application silently continues with an uninitialized texture. These should be replaced with explicit exceptions or at minimum logged errors.

### 4. No Text Rendering

There is currently no way to draw text. This is a fundamental missing feature for any real application. A minimum viable implementation would use STB TrueType (already available via the existing LWJGL dependency) to rasterize glyphs into a texture atlas and expose a `DrawContext.drawText(font, text, x, y, z, color, queue)` method.

### 5. No Scissor / Clipping Region

There is no way to restrict drawing to a sub-region of the screen. This is essential for implementing UI elements like scrollable lists, windows, and panels. GLWrapper should expose a simple `Renderer.pushScissor(x, y, width, height)` / `Renderer.popScissor()` API backed by `glScissor`.

### 6. Texture Count Hardcap

`Config.TEXTURE_AMOUNT = 16` caps the number of textures per draw call at 16 and is not user-configurable. On some hardware the actual limit is lower than 16, and on modern hardware it may be far higher. This value should be queried from the driver at runtime via `glGetInteger(GL_MAX_TEXTURE_IMAGE_UNITS)` rather than hardcoded.

### 7. No Window Resize Integration

`Renderer.updateProjMatrix(width, height)` must be called manually every time the window is resized. GLWrapper could optionally register a GLFW framebuffer size callback itself, or at minimum clearly document what happens if this call is missed (everything stretches or renders at the wrong scale).

---

## Nice-to-Haves

These would meaningfully expand the library's usefulness without changing its fundamental character.

### 8. More DrawContext Shapes

The current `DrawContext` only covers rectangles, gradients, and textures. Common additions:

- `drawLine(x1, y1, x2, y2, z, thickness, color, queue)` — thick lines rendered as quads, since OpenGL's native line width is limited and deprecated on modern drivers.
- `drawCircle(cx, cy, z, radius, segments, color, queue)` — approximated via `TRIANGLE_FAN`.
- `drawRoundedRect(x1, y1, x2, y2, z, radius, color, queue)` — extremely common in modern UIs.
- `drawOutlineRect(x1, y1, x2, y2, z, thickness, color, queue)` — a rectangle border without fill.

### 9. A Proper Color API

Colors are currently passed as packed ARGB integers (`0xFF_FF0000`). This is compact but error-prone — the ARGB byte order is not obvious, and the color-channel float API on `BufferBuilder` (which takes alpha first) differs from the integer API (which follows ARGB). A small `Color` class or record with named constructors (`Color.ofRgb(r, g, b)`, `Color.ofHsv(h, s, v)`, `Color.lerp(a, b, t)`) and built-in named constants would be a significant ergonomic improvement.

### 10. Transform Stack

There is currently no way to apply a translation, rotation, or scale to a group of vertices without computing the transformed coordinates manually. A matrix stack — `Renderer.pushMatrix()`, `Renderer.translate(x, y)`, `Renderer.rotate(angle)`, `Renderer.scale(sx, sy)`, `Renderer.popMatrix()` — would unlock a wide range of use cases (animated UI, rotated sprites, zoomed viewports) with minimal complexity.

### 11. Configurable Texture Filtering

Textures are hardcoded to `GL_NEAREST` filtering (pixel art / sharp edges). There is no way to use `GL_LINEAR` (smooth/bilinear) filtering or mipmaps. The `DrawContext.drawTexture` overloads should accept an optional `TextureFilter` parameter, or the `Identifier`-based texture system should support a filtering hint.

### 12. Framebuffer / Render-to-Texture

Rendering into an offscreen texture and then drawing that texture on screen is a prerequisite for effects like blur, shadows, glow, and off-screen UI compositing. A `Framebuffer` abstraction with `bind()` / `unbind()` and an `Identifier`-compatible result texture would fit naturally into the existing API.

### 13. Shader Hot-Reload

During development, iterating on custom shaders requires restarting the application every time a GLSL file changes. A `ShaderProgram.reload()` method, or a `Renderer.enableShaderHotReload()` development mode that watches shader files and automatically recompiles them on change, would dramatically speed up shader authoring.

---

## Crazy Yet Game-Changing Ideas

These are larger bets. Each would require significant design work, but any one of them could make GLWrapper stand out from other LWJGL wrappers.

### 14. Retained Mode Scene Graph

Right now GLWrapper is entirely **immediate mode** — you describe every frame from scratch every loop iteration. This is simple, but inefficient for content that doesn't change. A retained mode layer would let you declare a tree of persistent nodes (`RectNode`, `TextNode`, `ImageNode`, etc.) with observable properties. The renderer then diffs the tree between frames and only re-submits the geometry that actually changed. This is essentially what every modern UI framework (JavaFX, Flutter, React) does under the hood, and it would allow GLWrapper to target high-frame-rate UIs without burning CPU rebuilding static content every tick.

### 15. GPU-Driven Instanced Rendering

Currently, drawing 1000 identical sprites requires 1000 sets of vertex data — the same quad geometry repeated 1000 times in the buffer. OpenGL's instanced rendering (`glDrawElementsInstanced`) allows the geometry to be uploaded once while per-instance data (position, color, UV offset) is supplied in a separate small buffer. GLWrapper could expose this as a `InstancedBufferBuilder` where you provide the template geometry once and then call `.instance(x, y, ...)` for each copy. At scale, this can reduce draw call data by 99% and is the standard technique for particle systems, tilemaps, and entity rendering in games.

### 16. A Java DSL That Generates GLSL

Writing GLSL is the single biggest barrier for users who want custom visuals but don't know shader languages. A Java-based shader DSL — where you write shader logic as Java lambdas or expression trees that get compiled to GLSL at startup — would make custom effects accessible without ever opening a `.fsh` file. The idea: you write something like `ShaderDSL.fragment(color -> color.multiply(ShaderDSL.uniform("uBrightness", Float.class)))`, and GLWrapper generates, compiles, and links the GLSL program for you. This would be genuinely novel in the Java/LWJGL ecosystem.
