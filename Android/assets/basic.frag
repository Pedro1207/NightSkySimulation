#version 310 es

precision highp float;

uniform sampler2D tex;

out vec4 outputF;
in vec2 texCoord;

void main()
{
	vec4 color = texture(tex, texCoord);
	outputF = color;
}