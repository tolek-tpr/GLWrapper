#version 450 core

layout(location = 0) in vec3 aPos;
layout(location = 2) in vec3 aUVI;
layout(location = 3) in vec3 aNormal;
layout(location = 4) in int aModelIdx;

layout(std430, binding = 0) buffer Matrices {
    mat4 modelMatrices[];
};

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

out vec3 fUVI;
flat out int fModelIdx;

void main() {
    gl_Position = projMatrix * viewMatrix * modelMatrices[aModelIdx] * vec4(aPos, 1.0);
    fUVI = vec3(vec2(vec4(aUVI.xy, 1, 0) * modelMatrices[aModelIdx]), aUVI.z);
    fModelIdx = aModelIdx;
}