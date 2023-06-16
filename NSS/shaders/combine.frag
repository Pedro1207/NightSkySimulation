#version 430

uniform	vec4 diffuse;

in vec2 texCoord;

uniform sampler2D temp_rt_texture;
uniform sampler2D final_rt_texture;

out vec4 outputF;

float map(float value, float min1, float max1, float min2, float max2) {
  return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
}

void main()
{
	vec3 skyColor = texture(final_rt_texture, texCoord).xyz;
	vec3 starColor = texture(temp_rt_texture, texCoord).xyz;

	float luminance = 0.2126 * skyColor.x + 0.7152 * skyColor.y + 0.0722 * skyColor.z;
	float factor;
	float cutoff = 0.5;
	if(luminance > cutoff){
		factor = 0;
	}
	else{
		factor = map(luminance, 0, cutoff, 1, 0);
	}
	outputF = vec4(skyColor + starColor * factor, 1);
}