#version 150

// Mistly Client motion blur before 1.21.2: the new frame mixed with the last ones (Strength of the last image kept)
uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;

uniform float Strength;

in vec2 texCoord;

out vec4 fragColor;

void main(){
    vec4 current = texture(DiffuseSampler, texCoord);
    vec4 previous = texture(PrevSampler, texCoord);
    fragColor = vec4(mix(current.rgb, previous.rgb, Strength), 1.0);
}
