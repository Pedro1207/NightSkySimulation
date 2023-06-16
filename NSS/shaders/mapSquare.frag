#version 430

uniform	vec4 diffuse;

uniform sampler2D circle_tex;

out vec4 outputF;

in vec2 texCoord;

void main()
{

	vec4 diffuse;
	diffuse = texture(circle_tex, texCoord);
	if(diffuse.x == 0){
		discard;
	}
	else{
		outputF = vec4(diffuse.xyz, 1);
	}
}