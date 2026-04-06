# Technical Decisions

A summary of notable technical decisions made in GLWrapper, written to be accessible to developers without an OpenGL background.

---

### Memory-Mapped GPU Buffers

Rather than uploading vertex data to the GPU each frame using the standard `glBufferData` call, GLWrapper maps a region of GPU memory directly into CPU address space and writes into it. Think of it like writing directly onto the GPU's notepad instead of handing it a new piece of paper every frame. This avoids an expensive copy step and reduces the chance of the CPU and GPU blocking each other waiting for data to transfer.

### Ring Buffer with Three Segments

The mapped GPU buffer is structured as a **ring buffer** with three slots. Because the GPU processes frames slightly behind the CPU, a single buffer risks the CPU overwriting data the GPU hasn't finished reading yet. Three slots give the GPU enough runway to finish its work while the CPU fills the next slot — the same idea as triple buffering a display.

### Automatic Index Generation

In raw OpenGL, you must manually provide an index list that tells the GPU in which order to connect your vertices into triangles. GLWrapper generates these indices automatically based on the vertex count and draw mode, so you never have to think about them.

### Buffer Pool

`BufferBuilder` objects are expensive to create because they allocate GPU memory and set up GPU state. `Buffers.getBuffer()` maintains a static pool keyed by draw mode and vertex format, so that objects are created once and reused every frame rather than allocated and discarded.

### Non-Interleaved Attribute Layout

Per-vertex data (position, color, UV) is stored in the GPU buffer as separate contiguous blocks — all positions first, then all colors, then all UVs — rather than interleaved `[pos, color, pos, color, ...]`. This is a deliberate layout choice that simplifies the internal memory copy logic.

### Orthographic Projection Built In

OpenGL natively works in a normalized coordinate space where the entire screen spans from -1 to 1 on each axis. GLWrapper installs an orthographic projection matrix automatically, converting that space into pixel coordinates. This is why you can write `vertex(400, 300, 0)` and have it mean "the center of an 800×600 window" without any math.

### `Immediate` as a Behavioral Override

`Immediate` is a subclass of `BufferBuilder` that overrides a single method — `addToQueue()` — to call `Renderer.renderBuffer()` directly instead. The rest of the class is inherited unchanged. This is a clean example of using inheritance for behavioral specialization rather than code sharing.

### String-Keyed Attribute Registry

Custom vertex attributes are identified by string names registered in a static `HashMap` (`AttributeType.register("MY_ATTR")`). This allows user code to extend the attribute system without touching any enum or modifying GLWrapper internals, at the cost of losing compile-time safety on the names.
