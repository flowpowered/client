// $shader_type: vertex

#version 330

layout(location = 0) in vec3 position;

out vec2 textureUV;
noperspective out vec3 viewRay;
out vec3 lightPositionView;

uniform mat4 viewMatrix;
uniform vec3 lightPosition;
uniform float tanHalfFOV;
uniform float aspectRatio;

void main() {
    textureUV = (position.xy + 1) / 2;

    viewRay = vec3(position.x * tanHalfFOV * aspectRatio, position.y * tanHalfFOV, -1);

    lightPositionView = (viewMatrix * vec4(lightPosition, 1)).xyz;

    gl_Position = vec4(position, 1);
}
