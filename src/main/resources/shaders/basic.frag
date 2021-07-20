#version 450

layout(location = 0) in vec3 baseNormal;
layout(location = 1) in vec2 texCoordinates;

layout(location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D colorTextureSampler;
layout(set = 0, binding = 2) uniform sampler2D heightTextureSampler;

void main() {
    // TODO Use lighting mathematics
    float delta = 0.001;
    float deltaDistance = delta * 1.0;
    float heightLowX = texture(heightTextureSampler, texCoordinates - vec2(delta, 0.0)).r;
    float heightHighX = texture(heightTextureSampler, texCoordinates + vec2(delta, 0.0)).r;
    float heightLowZ = texture(heightTextureSampler, texCoordinates - vec2(0.0, delta)).r;
    float heightHighZ = texture(heightTextureSampler, texCoordinates + vec2(0.0, delta)).r;

    float heightDiffX = heightHighX - heightLowX;
    float heightDiffZ = heightHighZ - heightLowZ;
    vec3 heightVecX = vec3(2.0 * deltaDistance, heightDiffX, 0.0);
    vec3 heightVecZ = vec3(0.0, heightDiffZ, 2.0 * deltaDistance);

    vec3 heightNormal = normalize(cross(heightVecZ, heightVecX));

    vec4 textureColor = texture(colorTextureSampler, texCoordinates);
    outColor = vec4(0.5 * heightNormal + vec3(0.5, 0.5, 0.5), 1.0);
}
