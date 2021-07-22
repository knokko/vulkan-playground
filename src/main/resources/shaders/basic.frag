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

struct MaterialProperties {
    float ambientValue;

    float diffuseMaterialFactor;
    float diffuseLightFactor;

    float specularMaterialFactor;
    float specularLightFactor;
    float shininess;
} MATERIALS[] = {
    // The first material is terrain material. This doesn't have any specular lighting
    {
        0.2, // ambientValue
        0.8, // diffuseMaterialFactor
        0.0, // diffuseLightFactor
        0.0, // specularMaterialFactor
        0.0, // specularLightFactor
        0.0 // shininess
    },
    // The second material is plastic material. This has both diffuse and specular lighting
    {
        0.1, // ambientValue
        0.6, // diffuseMaterialFactor
        0.0, // diffuseLightFactor
        0.0, // specularMaterialFactor
        0.3, // specularLightFactor
        20.0 // shininess
    },
    // The third material is metal. This has mostly specular lighting
    {
        0.1, // ambientValue
        0.3, // diffuseMaterialFactor
        0.0, // diffuseLightFactor
        0.6, // specularMaterialFactor
        0.0, // specularLightFactor
        90.0 // shininess
    }
};

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

    MaterialProperties material = MATERIALS[0];
    vec3 textureColor = texture(colorTextureSampler, texCoordinates).rgb;
    vec3 lightColor = vec3(1.0, 1.0, 1.0);

    vec3 ambientColor = textureColor * material.ambientValue;
    vec3 diffuseColor = material.diffuseMaterialFactor * textureColor + material.diffuseLightFactor * lightColor;
    vec3 specularColor = material.specularMaterialFactor * textureColor + material.specularLightFactor * lightColor;
    float shininess = material.shininess;

    vec3 toLight = normalize(vec3(100, 20, 10));
    vec3 toView = normalize(camera.position - worldPosition);

    float dotLightNormal = dot(toLight, heightNormal);
    vec3 resultColor = ambientColor;

    if (dotLightNormal > 0.0) {
        resultColor += diffuseColor * dotLightNormal;

        vec3 reflectedLight = normalize(2.0 * dotLightNormal * heightNormal - toLight);
        float dotReflectedView = dot(reflectedLight, toView);
        if (dotReflectedView > 0.0) {
            resultColor += specularColor * pow(dotReflectedView, shininess);
        }
    }

    outColor = vec4(resultColor, 1.0);
}
