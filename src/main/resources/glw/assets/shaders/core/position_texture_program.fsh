#version 450 core

in vec3 fUVI;

uniform sampler2D uTextures[8];

out vec4 FragColor;

void main() {
    int idx = int(fUVI.z);
    FragColor = texture(uTextures[idx], vec2(fUVI.xy));
}