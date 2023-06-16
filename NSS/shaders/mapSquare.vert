#version 430

uniform	mat4 m_pvm;
uniform mat4 m_p;

uniform float ratio;

in vec4 position;	// local space
in vec2 texCoord0;

out vec2 texCoord;


void main () {
	texCoord = texCoord0;
	
	vec4 pos = position;
	pos.x = pos.x / ratio;

	pos *= 0.75;
	pos.w = 1.0;

	gl_Position = pos;	
}