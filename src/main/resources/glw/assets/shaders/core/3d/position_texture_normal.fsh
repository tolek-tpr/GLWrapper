#version 450 core

struct Material {
    vec4 color;
    vec4 emissiveColor;
    float metallic;
    float roughness;
};

layout(std430, binding = 1) buffer Materials {
    Material materials[];
};

uniform sampler2D uTextures[8];

in vec3 fUVI;
flat in int fModelIdx;

out vec4 FragColor;

void main() {
    FragColor = texture(uTextures[int(fUVI.z)], vec2(fUVI.xy)) * materials[fModelIdx].color;
}