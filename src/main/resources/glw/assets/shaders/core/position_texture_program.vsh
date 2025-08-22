#version 450 core

layout(location = 0) in vec3 aPos;
layout(location = 2) in vec3 aUVI;

out vec3 fUVI;

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

void main() {
    gl_Position = projMatrix * viewMatrix * vec4(aPos, 1);
    fUVI = aUVI;
}