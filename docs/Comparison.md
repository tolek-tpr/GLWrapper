# Comparison between GLWrapper and pure OpenGL
This file aims to show and describe how much easier using GLWrapper is rather than traditional OpenGL.
I have provided 2 files which show the difference clearly.

## The task:
The task is, to create an app that draws a rectangle in the middle of the screen where each vertex is a different color.
To see the difference, look at the GLWrapper.java file which uses the GLWrapper library to draw said rectangle and
RawOpenGL.java which uses just LWJGL to draw the exact same thing. The actual rendering code with GLWrapper is just
8 lines and the rest is just for setting up the window! The actual rendering code is visible here:
```
Renderer.updateProjMatrix(800, 600);

loop {
    BufferBuilder builder = Buffers.getBuffer(DrawMode.TRIANGLE_STRIP, DrawMode.VertexFormat.POSITION_COLOR);

    builder.vertex(200, 400, 0).color(1, 0, 1, 0);
    builder.vertex(600, 400, 0).color(1, 1, 0, 0);
    builder.vertex(200, 200, 0).color(1, 0, 0, 1);
    builder.vertex(600, 200, 0).color(1, 1, 1, 0);
    builder.addToQueue();

    Renderer.render();
}
```
Compared to OpenGL, where you have to manually register a VBO, VAO and EBO, then compile the shaders, set up the data, etc. Which does
use ONLY 37 lines of actual rendering code, but it is easy to mess something very small up, which usually ends in nothing showing,
however with GLWrapper you probably won't manage do to such a thing (and if you do please open an issue ;) ). For example,
you could accidentally pass the vertex size to `glVertexAttribPointer` not in bytes but just the amount, same for the stride,
or you could accidentally pass the bytes instead of just the amount into the 2nd parameter of the same function. As you can
see, OpenGL has a TON of places where you can very easily shoot yourself in the foot and GLWrapper aims to make rendering as
easy as possible with no such cases. Not to mention that in regular OpenGL you do not have pre-made shader files which means you would
also need to create those yourself as opposed to GLWrapper which has basic built-in shaders.

If you are curious this is the actual rendering code with OpenGL (code excluding window setup):
```
int VAO, VBO, EBO;
VBO = glGenBuffers();
EBO = glGenBuffers();
VAO = glGenVertexArrays();

glBindVertexArray(VAO);

float[] vertices = {
        // POS                 // COLOR
        -0.5f, 0.5f, 0.0f,     1, 0, 0, 1,
        -0.5f, -0.5f, 0.0f,    0, 1, 0, 1,
        0.5f, -0.5f, 0.0f,     0, 0, 1, 1,
        0.5f, 0.5f, 0.0f,      1, 1, 0, 1
};

int[] indices = {
        0, 1, 2,
        2, 3, 0
};

glBindBuffer(GL_ARRAY_BUFFER, VBO);
glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

int VERTEX_SIZE_BYTES = 7 * Float.BYTES;
int POS_SIZE_BYTES = 3 * Float.BYTES;

glVertexAttribPointer(0, 3, GL_FLOAT, false, VERTEX_SIZE_BYTES, 0);
glVertexAttribPointer(1, 4, GL_FLOAT, false, VERTEX_SIZE_BYTES, POS_SIZE_BYTES);

glEnableVertexAttribArray(0);
glEnableVertexAttribArray(1);

glBindBuffer(GL_ARRAY_BUFFER, 0);

// Shader setup
String vCode = Utils.readFile(vertexInputStream);
String fCode = Utils.readFile(fragmentInputStream);

int vertex, fragment, shaderID;

vertex = glCreateShader(GL_VERTEX_SHADER);
glShaderSource(vertex, vCode);
glCompileShader(vertex);

fragment = glCreateShader(GL_FRAGMENT_SHADER);
glShaderSource(fragment, fCode);
glCompileShader(fragment);

shaderID = glCreateProgram();
glAttachShader(shaderID, vertex);
glAttachShader(shaderID, fragment);
glLinkProgram(shaderID);

glDeleteShader(vertex);
glDeleteShader(fragment);

loop {
    glBindBuffer(GL_ARRAY_BUFFER, VBO);
    glBindVertexArray(VAO);
    glUseProgram(shaderID);

    glDrawElements(GL_TRIANGLES, 4, GL_UNSIGNED_INT, 0);

    glBindVertexArray(0);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    glUseProgram(0);
}
```