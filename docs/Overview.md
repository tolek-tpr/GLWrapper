# GLWrapper

**OpenGL rendering for Java developers who have better things to do.**

---

OpenGL is powerful. It is also a 30-year-old C API that makes you manually manage memory buffers, compile shaders, wire up vertex attribute pointers, and calculate byte offsets — before a single pixel appears on screen. One misplaced multiplication and you get a blank window with no useful error message.

GLWrapper takes all of that and makes it disappear.

---

## What it looks like

Draw a colored triangle:

```java
BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, DrawMode.VertexFormat.POSITION_COLOR);
builder.vertex(100, 50,  0).color(1f, 1f, 0f, 0f);
builder.vertex(200, 250, 0).color(1f, 0f, 1f, 0f);
builder.vertex(0,   250, 0).color(1f, 0f, 0f, 1f);
builder.addToQueue();

Renderer.render();
```

That's it. No VAO setup. No shader compilation. No index buffer. No byte arithmetic. Just vertices.

---

## Why GLWrapper

**Coordinates that make sense.** Vertices are specified in screen pixels. `(0, 0)` is the bottom-left corner of the window. `(800, 600)` is the top-right. No normalized device coordinates, no mental gymnastics.

**Shaders included.** Every built-in vertex format ships with a matching shader. You never write a line of GLSL unless you want to.

**GPU-efficient by default.** Under the hood, GLWrapper uses memory-mapped ring buffers to stream vertex data to the GPU without redundant copies or CPU–GPU synchronisation stalls. Fast rendering is not something you have to earn.

**Escape hatches when you need them.** Custom fragment shaders, custom vertex attributes, custom uniforms — all supported, none required. Start simple, go deep only when the project demands it.

---

## From 37 lines to 8

This is the actual OpenGL code required to draw a rectangle with per-vertex colors — excluding window setup and shader source strings:

```
glGenBuffers / glGenVertexArrays / glBindVertexArray
glBindBuffer / glBufferData (×2)
glVertexAttribPointer / glEnableVertexAttribArray (×2)
glCreateShader / glShaderSource / glCompileShader (×2)
glCreateProgram / glAttachShader / glLinkProgram / glDeleteShader (×2)
... and the loop: glBindBuffer / glBindVertexArray / glUseProgram / glDrawElements / unbind everything
```

With GLWrapper, the same result is 8 lines — and the common mistakes that produce a blank screen (wrong byte stride, wrong attribute size unit, forgetting to unbind) simply cannot happen.

---

## Get started

GLWrapper is available via JitPack. Add it to your Gradle project, drop `Renderer.updateProjMatrix(width, height)` into your initialisation code and `Renderer.render()` at the end of your game loop, and you're rendering.

See [Setup.md](Setup.md) for dependency configuration and [Documentation.md](Documentation.md) for the full API.
