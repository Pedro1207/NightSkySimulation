#version 430

out vec4 outputF;

in vec3 normal;

void main()
{
	outputF = vec4(0.3, 0.6, 0.3, 0.0) * max(dot(normalize(normal), normalize(vec3(1.0, 1.0, 0.0))), 0.0);
}