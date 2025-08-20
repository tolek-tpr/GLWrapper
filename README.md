# GlWrapper
GlWrapper is an easy-to-use library for rendering with OpenGL. It provides built in buffered rendering as 
well as immediate rendering through the `BufferBuilder` and `Immediate` classes.

**DISCLAIMER**: You still need SOME OpenGL knowledge for more advanced stuff! 

## Setting up
Setting GlWrapper is extremely easy, you set up a clean LWJGL application and add a call to
`Renderer.render()` at the end of your game loop. GlWrapper also uses a projection matrix which by default
uses an orthographic projection. To set the projection matrix up, simply call `Renderer.updateProjMatrix(width, height)`
in the scale callback, or if you'd rather use a custom projection matrix, simply set `Renderer.projMatrix` which is a public field
whenever you need to.

## Rendering
The basic concept of GlWrapper is to use `BufferBuilder` and `Immediate` objects, there is also a `DrawContext` class
which has some helper functions for common shapes, gradients, etc.

A basic setup for using a `BufferBuilder` looks something like this:
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            builder.vertex(0, 0, 0).color(1, 1, 0, 0);
            builder.vertex(200, 0, 0).color(1, 0, 1, 0);
            builder.vertex(200, 200, 0).color(1, 0, 0, 1);
            builder.addToQueue();
            
            Renderer.render();
        }
    }
    
}
```
Let's go over what each part does. First we get a `BufferBuilder` object from a list of pre-made BufferBuilders by calling
`Buffers.getBuffer(DrawMode, VertexFormat)`. The arguments of this call determine what OpenGL primitive you want to use
as well as the vertex format you will be using. In this example it would be `DrawMode.TRIANGLES` and `VertexFormat.POSITION_COLOR`.
Next, we add 3 vertices by calling `BufferBuilder#vertex(x, y, z)` where x, y and z are in screen space. Since we are using a
position and color vertex format, we also call `BufferBuilder#color(a, r, g, b)` where each component is a float from 0 to 1.
Lastly, we call `BufferBuilder#addToQueue()` which simply tells the renderer to render this buffer builder. This method 
only needs to be called once and any other calls will be ignored.

### Using the DrawContext
The `DrawContext` class aims to add utility functions for drawing common shapes and other stuff.
A basic example of using the `DrawContext` would be something like this:
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            DrawContext.drawRect(0, 0, 300, 300, 0, true);
            
            Renderer.render();
        }
    }
    
}
```
Every function in the draw context will either take a `BufferBuilder` as a parameter or it won't. The function which does
not take in a `BufferBuilder` will automatically retrieve the correct `BufferBuilder` by calling `Buffers.getBuffer()` with the
appropriate arguments. The `drawRect` method takes in these arguments: `DrawContext.drawRect([BufferBuilder], x1, y1, x2, y2, z, queue)`.
All of these arguments should be fairly self-explanatory, the queue parameter simply calls `BufferBuilder#addToQueue` if true.

**NOTE**: You can also pass an `Immediate` object into the DrawContext when using the `DrawContext.drawRect([BufferBuilder], x1, y1, x2, y2, z, queue)`
method, however you should keep in mind, that when queue is set to true, it will be the same as calling `Immediate#end` which
draws the buffer right now.

## Immediates
Immediate buffers are buffers that instead of being queued get drawn as soon as you call `Immediate#end()` and get cleared.
This can be useful if you want to draw something before another thing, take this example, you draw something using
the `BufferBuilder` (which gets queued) but you want to draw something before it however you have to wait for something:
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            builder.vertex(0, 0, 0).color(1, 1, 0, 0);
            builder.vertex(200, 0, 0).color(1, 0, 1, 0);
            builder.vertex(200, 200, 0).color(1, 0, 0, 1);
            builder.addToQueue();
            
            // Waiting for something, say some positions which only get there after the BufferBuilder calls
            int x1 = fetchX();
            int y1 = fetchY();
            Immediate immediate = Buffers.getImmediate(DrawMode.TRIANGLES, VertexFormat.POSITION);
            immediate.vertex(x1, y1, 0);
            immediate.vertex(400, y1, 0);
            immediate.vertex(400, 400, 0);
            immediate.end();
            
            Renderer.render();
        }
    }
    
}
```
Looking at the code, the `BufferBuilder` should get rendered first, however since the `BufferBuilder` is a queued object and
`Immediate` is not, which means that in reality, the `Immediate` get's rendered first.

# Advanced GlWrapper
## Identifiers
Identifiers are the primary way GlWrapper handles resources. To make an Identifier you simply call:
`new Identifier(<namespace>, <location>)`. The namespace, is the name of the folder located in the resources folder
like for example an Identifier to the basic vertex shader would look something like this:
`new Identifier("glw", "shaders/core/position_program.vsh")`, and the file structure would be
`resouces/glw/assets/shaders/core/position_program.vsh`. The Identifier class automatically adds the "assets", and the first argument
specifies the namespace.

## Registering custom BufferBuilders and Immediates
In addition to the buffer builders provided by the `Buffers` class, you can also register your own `BufferBuilder` and
`Immediate` objects. Why would you need to do something like this? Well, if you want to use a custom fragment or even
vertex shader, by default GlWrapper picks the appropriate shader from a premade list, located in `ShaderProgramKeys`,
but if you want more control over how your shapes look, you might want to register a custom fragment shader.

### Adding a BufferBuilder with a custom fragment shader
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            builder.withFragmentShader(new Identifier("<namespace>", "path/to/shader.fsh"));
            
            builder.vertex(0, 0, 0).color(1, 1, 0, 0);
            builder.vertex(200, 0, 0).color(1, 0, 1, 0);
            builder.vertex(200, 200, 0).color(1, 0, 0, 1);
            builder.addToQueue();
            
            Renderer.render();
        }
    }
    
}
```
This will create a new `BufferBuilder` with the specified `DrawMode` and `VertexFormat` and a custom fragment shader,
provided by the identifier. This allows you to further control the look of the triangle. You can also pass your custom 
`BufferBuilder` to the `DrawContext` if you wish to draw something with the DrawContext using a custom fragment shader like so:
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            builder.withFragmentShader(new Identifier("<namespace>", "path/to/shader.fsh"));
            
            DrawContext.drawRect(builder, 0, 0, 300, 300, 0, true);
            
            Renderer.render();
        }
    }
    
}
```
If you would like to register a custom `Immediate` the process is basically the same. You create an `Immediate` object
like you do with the `BufferBuilder` and call the same function as on the `BufferBuilder`.
You can also register entirely custom shaders (so you use a custom vertex and fragment shader) by doing something like this:
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            builder.withShader(new ShaderProgram(new Identifier("<namespace>", "path/to/shader.vsh"),
                    new Identifier("<namespace>", "path/to/shader.fsh")));
            
            DrawContext.drawRect(builder, 0, 0, 300, 300, 0, true);
            
            Renderer.render();
        }
    }
    
}
```