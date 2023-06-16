#version 430

uniform float maxStarBright;
uniform float minStarBright;

uniform sampler2D star_texture;

layout(std430, binding = 1) buffer stars {
	vec4 starPosition[];
};

out vec4 outputF;

in vec2 texCoord;
in float magnitude;

float magnitudeToLux(float magnitude){
	return pow(10, (-14.18-magnitude)/2.5);
}


float processingMapClamped(float value, float min1, float max1, float min2, float max2) {
	float mapped = min2 + (value - min1) * (max2 - min2) / (max1 - min1);
	if (mapped < 0) mapped = 0;
	if (mapped > 1) mapped = 1;
	return mapped;
}

void main()
{

	float maxLux = magnitudeToLux(maxStarBright);
	float minLux = magnitudeToLux(minStarBright);
	float magnitudeLux = magnitudeToLux(magnitude);

	vec4 color = texture(star_texture, texCoord);
	color = color * processingMapClamped(magnitudeLux, minLux, maxLux, 1, 0);
	outputF = vec4(color.xyz,1.0);
}