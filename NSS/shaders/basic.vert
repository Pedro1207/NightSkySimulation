#version 430

uniform	mat4 m_pvm;
uniform	mat3 m_normal;


in vec4 position;	// local space
in vec3 a_normal;

out vec3 normal;

void main () {

	normal = m_normal * a_normal;
	
	gl_Position = m_pvm * position;	
}