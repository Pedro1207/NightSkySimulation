#version 430

uniform sampler2D mercury_texture;
uniform sampler2D venus_texture;
uniform sampler2D mars_texture;
uniform sampler2D jupiter_texture;
uniform sampler2D saturn_texture;
uniform sampler2D uranus_texture;
uniform sampler2D neptune_texture;

out vec4 outputF;

in vec4 light_dir;
in vec3 outNormal;
flat in int planetIndex;
in vec2 texCoord;




float processingMapClamped(float value, float min1, float max1, float min2, float max2) {
	float mapped = min2 + (value - min1) * (max2 - min2) / (max1 - min1);
	if (mapped < 0) mapped = 0;
	if (mapped > 1) mapped = 1;
	return mapped;
}

void main()
{
	vec4 diffuse;
	switch(planetIndex){
		case 0: 
			diffuse = texture(mercury_texture, texCoord);
			break;
		case 1: 
			diffuse = texture(venus_texture, texCoord);
			break;
		case 2: 
			diffuse = texture(mars_texture, texCoord);
			break;
		case 3: 
			diffuse = texture(jupiter_texture, texCoord);
			break;
		case 4: 
			diffuse = texture(saturn_texture, texCoord);
			break;
		case 5: 
			diffuse = texture(uranus_texture, texCoord);
			break;
		case 6: 
			diffuse = texture(neptune_texture, texCoord);
			break;

	}

	float diff = max(dot(outNormal, light_dir.xyz), 0.0);
	
	outputF = vec4(diff * diffuse.xyz, 1.0);
}