#version 450 core

in vec4 fColor;
in float fs;

out vec4 FragColor;

void main() {
    FragColor = vec4(fs, fs, fs, 1) * fColor;
}