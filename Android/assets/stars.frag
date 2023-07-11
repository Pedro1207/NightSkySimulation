#version 310 es

precision highp float;

uniform float maxStarBright;
uniform float minStarBright;

uniform sampler2D star_texture;

out vec4 outputF;

in vec2 texCoord;
in float magnitude;


float processingMapClamped(float value, float min1, float max1, float min2, float max2) {
	float mapped = min2 + (value - min1) * (max2 - min2) / (max1 - min1);
	if (mapped < max2) mapped = 0.0;
	if (mapped > min2) mapped = 1.0;
	return mapped;
}

float magnitudeToLux(float magnitude){
	return pow(10.0, (-14.18-magnitude)/2.5);
}

void main()
{

	float magnitudeLux = magnitudeToLux(magnitude);

	vec4 color = texture(star_texture, texCoord);
	float dim = processingMapClamped(magnitudeLux, 7.0, 2.0, 1.0, 0.0);
	vec4 colorDimmed = color * dim;


	if (color.x + color.y + color.z > 0.0){
		outputF = vec4(colorDimmed.xyz,1.0);
	}
	else{
		discard;
	}
}