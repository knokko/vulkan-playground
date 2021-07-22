#version 460

layout(location = 0) in vec3 inBasePosition;
layout(location = 1) in vec3 inBaseNormal;
layout(location = 2) in vec2 inTexCoordinates;
layout(location = 3) in int inMatrixIndex;

layout(location = 0) out vec3 worldPosition;
layout(location = 1) out vec3 passBaseNormal;
layout(location = 2) out vec2 passTexCoordinates;

layout(set = 0, binding = 0) uniform Camera {
    mat4 matrix;
    vec3 position;
} camera;
layout(set = 0, binding = 2) uniform sampler2D heightTextureSampler;
layout(set = 0, binding = 3) readonly buffer Objects {
    mat4 transformationMatrices[];
} objects;

void main() {
    mat4 transformationMatrix = objects.transformationMatrices[inMatrixIndex + gl_BaseInstance];
    float extraHeight = texture(heightTextureSampler, inTexCoordinates).r;
    vec3 improvedPosition = inBasePosition + inBaseNormal * extraHeight;
    vec4 transformedPosition = transformationMatrix * vec4(improvedPosition, 1.0);
    gl_Position = camera.matrix * transformedPosition;

    worldPosition = transformedPosition.xyz;
    passBaseNormal = (transformationMatrix * vec4(inBaseNormal, 0.0)).xyz;
    passTexCoordinates = inTexCoordinates;
}
