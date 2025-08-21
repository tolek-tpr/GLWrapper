#version 450 core

in vec4 fColor;
in float fs;

out vec4 FragColor;

uniform float time;

void main() {
    FragColor = vec4(fs, fs, fs, 1) * fColor * vec4(abs(sin(time)));
}