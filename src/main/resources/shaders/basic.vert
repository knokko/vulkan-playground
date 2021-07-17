#version 460

layout(location = 0) in vec3 inBasePosition;
layout(location = 1) in vec3 inBaseNormal;
layout(location = 2) in vec2 inTexCoordinates;
layout(location = 3) in int inMatrixIndex;

layout(location = 0) out vec3 passBaseNormal;
layout(location = 1) out vec2 passTexCoordinates;

layout(set = 0, binding = 0) uniform Camera {
    mat4 matrix;
} camera;
layout(set = 0, binding = 1) readonly buffer Objects {
    mat4 transformationMatrices[];
} objects;

void main() {
    mat4 transformationMatrix = objects.transformationMatrices[inMatrixIndex + gl_BaseInstance];
    gl_Position = camera.matrix * transformationMatrix * vec4(inBasePosition, 1.0);

    passBaseNormal = (transformationMatrix * vec4(inBaseNormal, 0.0)).xyz;
    passTexCoordinates = inTexCoordinates;
}
