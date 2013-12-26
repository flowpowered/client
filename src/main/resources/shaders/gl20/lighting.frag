// $shader_type: fragment

// $texture_layout: colors = 0
// $texture_layout: normals = 1
// $texture_layout: depths = 2
// $texture_layout: materials = 3
// $texture_layout: occlusions = 4
// $texture_layout: shadows = 5

#version 120

varying vec2 textureUV;
varying vec3 viewRay;
varying vec3 lightPositionView;
varying vec3 spotDirectionView;

uniform sampler2D colors;
uniform sampler2D normals;
uniform sampler2D depths;
uniform sampler2D materials;
uniform sampler2D occlusions;
uniform sampler2D shadows;
uniform vec2 projection;
uniform float lightAttenuation;
uniform	float spotCutoff;

float linearizeDepth(float depth) {
    return projection.y / (depth - projection.x);
}

void main() {
    gl_FragColor = texture2D(colors, textureUV);

    vec4 rawNormalView = texture2D(normals, textureUV);
    if (rawNormalView.a <= 0) {
        return;
    }
    vec3 normalView = normalize(rawNormalView.xyz * 2 - 1);

    vec3 positionView = viewRay * linearizeDepth(texture2D(depths, textureUV).r);

    vec3 lightDifference = lightPositionView - positionView;
    float lightDistance = length(lightDifference);
    vec3 lightDirection = lightDifference / lightDistance;
    float distanceIntensity = 1 / (1 + lightAttenuation * lightDistance);

    float spotDotLight = dot(spotDirectionView, -lightDirection);
    float normalDotLight = dot(normalView, lightDirection);

    vec3 material = texture2D(materials, textureUV).rgb;

    float occlusion = texture2D(occlusions, textureUV).r;

    float shadow = texture2D(shadows, textureUV).r;

    float ambientTerm = material.z * occlusion;
    float diffuseTerm = 0;
    float specularTerm = 0;
    if (spotDotLight > spotCutoff && shadow > 0) {
        distanceIntensity *= (spotDotLight - spotCutoff) / (1 - spotCutoff);
        normalDotLight = max(0, normalDotLight);
        diffuseTerm = material.x * distanceIntensity * shadow * normalDotLight;
        if (normalDotLight > 0) {
            specularTerm = material.y * distanceIntensity * shadow * pow(max(0, dot(reflect(lightDirection, normalView), normalize(viewRay))), 20);
        }
    }

    gl_FragColor.rgb *= (diffuseTerm + specularTerm + ambientTerm);
}
