// $shader_type: fragment

// $texture_layout: weightedSum = 0
// $texture_layout: layerCount = 1

#version 120

varying vec2 textureUV;

uniform sampler2D weightedSum;
uniform sampler2D layerCount;

void main() {
    vec4 sum = texture2D(weightedSum, textureUV);
    float count = texture2D(layerCount, textureUV).r;

    if (count < 0.00001 || sum.a < 0.00001) {
        discard;
    }

    vec4 averageColor = vec4(sum.rgb / sum.a, sum.a / count);
    float destinationAlpha = pow(max(0, 1 - averageColor.a), count);
    gl_FragColor = vec4(averageColor.rgb, destinationAlpha);
}
