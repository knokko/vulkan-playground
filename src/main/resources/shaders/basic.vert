#version 460

layout(location = 0) in vec3 inBasePosition;
layout(location = 1) in vec3 inBaseNormal;
layout(location = 2) in vec2 inColorTexCoordinates;
layout(location = 3) in vec2 inHeightTexCoordinates;
layout(location = 4) in int inMatrixIndex;
layout(location = 5) in int inMaterialIndex;
layout(location = 6) in float inDeltaFactor;

layout(location = 0) out vec3 worldPosition;
layout(location = 1) out vec3 passBaseNormal;
layout(location = 2) out vec2 passColorTexCoordinates;
layout(location = 3) out vec2 passHeightTexCoordinates;
layout(location = 4) out int passMatrixIndex;
layout(location = 5) out int passMaterialIndex;
layout(location = 6) out float passDeltaFactor;

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
    float extraHeight = texture(heightTextureSampler, inHeightTexCoordinates).r;
    vec3 improvedPosition = inBasePosition + inBaseNormal * extraHeight;
    vec4 transformedPosition = transformationMatrix * vec4(improvedPosition, 1.0);
    gl_Position = camera.matrix * transformedPosition;

    worldPosition = transformedPosition.xyz;
    passBaseNormal = inBaseNormal;
    passColorTexCoordinates = inColorTexCoordinates;
    passHeightTexCoordinates = inHeightTexCoordinates;
    passMatrixIndex = inMatrixIndex + gl_BaseInstance;
    passMaterialIndex = inMaterialIndex;
    passDeltaFactor = inDeltaFactor;
}
