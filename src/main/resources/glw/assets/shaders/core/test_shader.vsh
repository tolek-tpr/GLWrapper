#version 450 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec4 aColor;

uniform mat4 projMatrix;

out vec4 fColor;

void main() {
    gl_Position = projMatrix * vec4(aPos, 1);
    fColor = aColor;
}