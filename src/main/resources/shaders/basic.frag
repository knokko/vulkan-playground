#version 450

layout(location = 0) in vec3 worldPosition;
layout(location = 1) in vec3 baseNormal;
layout(location = 2) in vec2 texCoordinates;

layout(location = 0) out vec4 outColor;

layout(set = 0, binding = 0) uniform Camera {
    mat4 matrix;
    vec3 position;
} camera;
layout(set = 0, binding = 1) uniform sampler2D colorTextureSampler;
layout(set = 0, binding = 2) uniform sampler2D heightTextureSampler;

void main() {
    float delta = 0.001;
    // TODO Stop hardcoding deltaDistance
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

    // TODO Stop hardcoding the light & material properties
    float ambientFactor = 0.1;
    float diffuseFactor = 0.5;
    float specularConstant = 0.2;
    float shininess = 1.0;

    vec3 toLight = normalize(vec3(100, 30, 10));
    vec3 toView = normalize(camera.position - worldPosition);

    float dotLightNormal = dot(toLight, heightNormal);
    vec3 reflectedLight = normalize(2.0 * dotLightNormal * heightNormal - toLight);
    float lightIntensity = 0.0;

    if (dotLightNormal > 0.0) {
        lightIntensity += diffuseFactor * dotLightNormal;
        float dotReflectedView = dot(reflectedLight, toView);
        if (dotReflectedView > 0.0) {
            lightIntensity += specularConstant * pow(dotReflectedView, shininess);
        }
    }

    if (lightIntensity < ambientFactor) {
        lightIntensity = ambientFactor;
    }

    vec4 textureColor = texture(colorTextureSampler, texCoordinates);
    outColor = vec4(min(lightIntensity, 1.0) * vec3(1.0, 1.0, 1.0), 1.0);
}
