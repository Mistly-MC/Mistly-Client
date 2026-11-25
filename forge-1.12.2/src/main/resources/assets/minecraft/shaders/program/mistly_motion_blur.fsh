#version 120

// Mistly Client motion blur: the new frame mixed with the last ones (Strength of the last image kept)
uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

varying vec2 texCoord;

uniform float Strength;

void main() {
    vec4 current = texture2D(DiffuseSampler, texCoord);
    vec4 previous = texture2D(PrevSampler, texCoord);
    gl_FragColor = vec4(mix(current.rgb, previous.rgb, Strength), 1.0);
}
