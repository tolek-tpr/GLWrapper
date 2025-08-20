#version 450 core

layout(location = 0) in vec3 aPos;

uniform mat4 projMatrix;

void main() {
    gl_Position = projMatrix * vec4(aPos, 1);
}