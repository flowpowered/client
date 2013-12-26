// $shader_type: vertex

// $attrib_layout: position = 0

#version 120

attribute vec3 position;

varying vec2 textureUV;
varying vec3 viewRay;
varying vec3 lightPositionView;
varying vec3 spotDirectionView;

uniform mat4 viewMatrix;
uniform mat4 normalMatrix;
uniform vec3 lightPosition;
uniform vec3 spotDirection;
uniform float tanHalfFOV;
uniform float aspectRatio;

void main() {
    textureUV = (position.xy + 1) / 2;

    viewRay = vec3(position.x * tanHalfFOV * aspectRatio, position.y * tanHalfFOV, -1);

    lightPositionView = (viewMatrix * vec4(lightPosition, 1)).xyz;

    spotDirectionView = normalize((normalMatrix * vec4(spotDirection, 0)).xyz);

    gl_Position = vec4(position, 1);
}
