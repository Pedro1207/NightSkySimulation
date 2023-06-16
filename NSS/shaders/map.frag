#version 430

uniform sampler2D earth_tex;

out vec4 outputF;

in vec2 texCoord;

void main()
{
	vec4 diffuse;
	diffuse = texture(earth_tex, texCoord);

	outputF = vec4(diffuse.xyz, 1.0);
}