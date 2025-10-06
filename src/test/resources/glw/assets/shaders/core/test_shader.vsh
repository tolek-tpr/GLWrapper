#version 450 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec4 aColor;
layout(location = 3) in float something;

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

out vec4 fColor;
out float fs;

void main() {
    gl_Position = projMatrix * viewMatrix * vec4(aPos, 1);
    fColor = aColor;
    fs = something;
}