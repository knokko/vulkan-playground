#version 450

layout(location = 0) in vec3 baseNormal;
layout(location = 1) in vec2 texCoordinates;

layout(location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D colorTextureSampler;

void main() {
    // TODO Use lighting mathematics
    outColor = texture(colorTextureSampler, texCoordinates);
}
