// $shader_type: vertex

// $attrib_layout: position = 0

#version 120

attribute vec3 position;

varying vec2 textureUV;
varying vec3 viewRay;
varying vec3 lightPositionView;

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
