#version 430

uniform	mat4 m_pvm;
uniform mat4 m_p;
uniform mat4 m_v;
uniform mat4 m_m;
uniform uint frame;

in vec4 position;	// local space
in vec2 texCoord0;

layout(std430, binding = 1) buffer stars {
	vec4 starPosition[];
};

out vec2 texCoord;
out float magnitude;

const float PI = 3.14159265358979323846;


mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    
    return transpose(mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                          oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                          oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                          0.0,                                0.0,                                0.0,                                1.0));
}

mat4 translationMatrix(vec3 translation)
{
    
    return transpose(mat4(1.0,  0.0,  0.0,  translation.x,
                          0.0,  1.0,  0.0,  translation.y,
                          0.0,  0.0,  1.0,  translation.z,
                          0.0,  0.0,  0.0,  1.0));
}


void main () {	

	int index = gl_InstanceID;
  vec4 starInfo = starPosition[index];
  texCoord = texCoord0;
  magnitude = starInfo.w;
  
  mat4 m_vm = m_v * m_m * translationMatrix(starInfo.xyz);
  for(int i = 0; i < 3; i++){
	  for(int j=0; j < 3; j++) {
		  if (i == j){
			  m_vm[i][j] = 1.0;
      }
		  else{
			  m_vm[i][j] = 0.0;
      }
	  }
  } 
  
  
	gl_Position = m_p * m_vm * position;	
}