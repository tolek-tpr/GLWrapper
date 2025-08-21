# GLWrapper
GLWrapper is an easy-to-use library for rendering with OpenGL. It provides built in buffered rendering as 
well as immediate rendering through the `BufferBuilder` and `Immediate` classes.

If you would like to check out how GLWrapper compares to raw OpenGL checkout [Comparison.md](docs/Comparison.md)

**DISCLAIMER**: You still need SOME OpenGL knowledge for more advanced stuff! 

## Setting up
Setting GLWrapper is extremely easy, you set up a clean LWJGL application and add a call to
`Renderer.render()` at the end of your game loop. GLWrapper also uses a projection matrix which by default
uses an orthographic projection. To set the projection matrix up, simply call `Renderer.updateProjMatrix(width, height)`
in the scale callback, or if you'd rather use a custom projection matrix, simply set `Renderer.projMatrix` which is a public field
whenever you need to.

## Rendering
The basic concept of GLWrapper is to use `BufferBuilder` and `Immediate` objects, there is also a `DrawContext` class
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

# Advanced GLWrapper
## Identifiers
Identifiers are the primary way GLWrapper handles resources. To make an Identifier you simply call:
`new Identifier(<namespace>, <location>)`. The namespace, is the name of the folder located in the resources folder
like for example an Identifier to the basic vertex shader would look something like this:
`new Identifier("glw", "shaders/core/position_program.vsh")`, and the file structure would be
`resouces/glw/assets/shaders/core/position_program.vsh`. The Identifier class automatically adds the "assets", and the first argument
specifies the namespace.

## Registering custom BufferBuilders and Immediates
In addition to the buffer builders provided by the `Buffers` class, you can also register your own `BufferBuilder` and
`Immediate` objects. Why would you need to do something like this? Well, if you want to use a custom fragment or even
vertex shader, by default GLWrapper picks the appropriate shader from a premade list, located in `ShaderProgramKeys`,
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
like you do with the `BufferBuilder` and call the same function as on the `BufferBuilder`, which you can see here:
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            Immediate immediate = new Immediate(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            immediate.withFragmentShader(new Identifier("<namespace>", "path/to/shader.fsh"));
            
            DrawContext.drawRect(immediate, 0, 0, 300, 300, 0, true);
            
            Renderer.render();
        }
    }
    
}
```
### Adding a BufferBuilder with a custom vertex and fragment shader
You can also register entirely custom shaders (so you use a custom vertex and fragment shader) by doing something like this:

**NOTE**: You can also set custom shaders on the default BufferBuilders provided by the `Buffers` class by calling the same methods. 
This is generally not recommended as it can lead to using the wrong shader later when trying to draw something with the
default shader for that buffer if you forget to reset it.
To reset the shader to the default for its vertex format, simply call `BufferBuilder#withShader(null)`
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
## Registering custom Vertex Attributes
To register a custom vertex attribute, you will need a few things. A custom vertex shader, a custom buffer and most
importantly, a custom `AttributeContainer`. Registering each of them is fairly simple and looks like this:
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // Registering a custom builder with your shader
            BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            builder.withShader(new ShaderProgram(new Identifier("<namespace>", "path/to/shader.vsh"),
                    new Identifier("<namespace>", "path/to/shader.fsh")));

            // Registering a custom attribute type and attribute container
            BufferBuilder.AttributeType type = BufferBuilder.AttributeType.register("TEST_TYPE");
            BufferBuilder.AttributeContainer customContainer = new BufferBuilder.AttributeContainer(type, 1, GlNumberType.FLOAT, 3);
            customBuilder.withVertexAttribute(customContainer);

            customBuilder.vertex(255, 255, 0)  .color(1, 1, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 255, 0)    .color(1, 0, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 0, 0)      .color(1, 1, 0, 0).attrib(type, 1f);
            
            Renderer.render();
        }
    }
    
}
```
Let's go through each new line step by step. You should already be familiar with registering a custom `BufferBuilder` with
your own shader. Next you need to register an `AttributeType` object, which allows GLWrapper to create a custom VBO 
automatically, after you have your type which you will use to both create a `AttributeContainer` and to write data to said
container, you need to actually create your `AttributeContainer`. To do this, simply call new on `AttributeContainer` like so:
```java
AttributeContainer container = new AttributeContainer(AttributeType, size, GlNumberType, location);
```
The `AttributeType` parameter should be self-explanatory, the next parameter is the size, or how many of your data type
should go in per vertex, in this example the size is 1 since I only want to provide one float to my vertex shader, I 
specify 1 as the size. Next is the `GlNumberType`, which is a wrapper for most of the `GL_FLOAT`, `GL_INT` etc.
It simply specifies the object type that you will receive in the vertex shader. The last argument is the location, or basically
what you put in the vertex shader to receive your vertex attribute like this:
```glsl
layout(location = X) in <TYPE> <NAME>
```
So in this case it would look something like this:
```glsl
layout(location = 3) in float aFloat;
```
Since I set the location to 3 and the `GlNumberType` to float this is what I will use.

**NOTE**: If you want your vertex attribute to be normalized, you have to call `AttributeContainer#setNormalized(true)`

## Registering custom uniforms
Registering a custom uniform is similar to registering a custom vertex attribute in that you also need to create a
custom `BufferBuilder` with your own shader, but this time you do not need to register an `AttributeType` or `AttributeContainer`,
but rather just call `BufferBuilder#withUniform(UniformProvider)` as shown in the example:
```java
public class App {
    
    public void loop() {
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // Registering a custom builder with your shader
            BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            builder.withShader(new ShaderProgram(new Identifier("<namespace>", "path/to/shader.vsh"),
                    new Identifier("<namespace>", "path/to/shader.fsh")));

            // Registering a custom uniform
            UniformProvider myUniformProvider = new UniformProvider() {
                
                @Override
                public void apply(ShaderProgram program) {
                    program.uniformFloat("time", (float) glfwGetTime());
                }
                
            };
            
            // Actually adding it to the BufferBuilder object
            builder.withUniform(myUniformProvider);

            customBuilder.vertex(255, 255, 0)  .color(1, 1, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 255, 0)    .color(1, 0, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 0, 0)      .color(1, 1, 0, 0).attrib(type, 1f);
            
            Renderer.render();
        }
    }
    
}
```
This is a fairly simple example of just setting a time uniform, however you can do much more by just changing the way you
create and handle the logic within `UniformProvider`. You might also want to make custom classes for uniforms that are common
like for example `ProjectionMatrixUniformProvider` which is by default on every `VertexFormat` so that you can use
screen space coordinates instead of NDC coordinates as discussed earlier. This is a small example of doing just that but with
a view matrix:
```java
public class App {
    
    public void loop() {
        Matrix4f viewMatrix = new Matrix4f(); // Set up your view matrix
        
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // Registering a custom builder with your shader
            BufferBuilder builder = new BufferBuilder(DrawMode.TRIANGLES, VertexFormat.POSITION_COLOR);
            builder.withShader(new ShaderProgram(new Identifier("<namespace>", "path/to/shader.vsh"),
                    new Identifier("<namespace>", "path/to/shader.fsh")));
            
            // Add your custom uniform
            builder.withUniform(new ViewMatrixUniformProvider(viewMatrix));

            customBuilder.vertex(255, 255, 0)  .color(1, 1, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 255, 0)    .color(1, 0, 1, 0).attrib(type, 0f);
            customBuilder.vertex(0, 0, 0)      .color(1, 1, 0, 0).attrib(type, 1f);
            
            Renderer.render();
        }
    }
    
    public record ViewMatrixUniformProvider(Matrix4f viewMatrix) implements UniformProvider {
        
        @Override
        public void apply(ShaderProgram program) {
            program.uniformMat4("viewMatrix", viewMatrix);
        }
        
    }
    
}
```
