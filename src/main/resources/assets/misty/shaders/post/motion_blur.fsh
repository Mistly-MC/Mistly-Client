#version 330
${glsl_extension}

// Motion blur of Misty Client: the new image of the world mixed with the last one shown.
uniform sampler2D InSampler;
uniform sampler2D PreviousSampler;

layout(std140) uniform MotionBlurConfig {
    // share of the last image kept (0: none, 0.9: a long trail)
    float Strength;
};

${glsl_in} vec2 texCoord;

${glsl_out} vec4 fragColor;

void main() {
    vec3 now = texture(InSampler, texCoord).rgb;
    vec3 before = texture(PreviousSampler, texCoord).rgb;
    fragColor = vec4(mix(now, before, Strength), 1.0);
}
