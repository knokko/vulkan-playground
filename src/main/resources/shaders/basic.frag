#version 450

layout(location = 0) in vec3 baseNormal;
layout(location = 1) in vec2 texCoordinates;

layout(location = 0) out vec4 outColor;

void main() {
    // TODO Use a texture and lighting mathematics
    outColor = vec4(texCoordinates.xy, 0.0, 1.0);
}
