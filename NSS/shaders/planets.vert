#version 430

uniform	mat4 m_pvm;
uniform	mat4 m_view;
uniform	mat3 m_normal;
uniform float planetScale;

uniform sampler2D mpa_data;

in vec4 position;
in vec3 normal;		
in vec2 texCoord0;


layout(std430, binding = 1) buffer planets {
	vec4 planetPosition[];
};

out vec4 light_dir;
out vec3 outNormal;
flat out int planetIndex; 
out vec2 texCoord;


const float PI = 3.14159265358979323846;
const int AU = 149598000;


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

mat4 scaleMatrix(vec3 scale)
{
    
    return transpose(mat4(scale.x, 0.0,      0.0,     0.0,
                          0.0,     scale.y,  0.0,     0.0,
                          0.0,     0.0,      scale.z, 0.0,
                          0.0,     0.0,      0.0,     1.0));
}

void main () {	

	int index = gl_InstanceID;
  vec4 planetInfo = planetPosition[index];
  vec4 planetInfo2 = planetPosition[7 + index];
  planetIndex = index;
  texCoord = texCoord0;

  light_dir = normalize(m_view * vec4(normalize(planetInfo2.xyz), 0.0));

  outNormal = normalize(m_normal * normal);

  float scale = planetScale * planetInfo.w;
	gl_Position = m_pvm * translationMatrix(planetInfo.xyz) * scaleMatrix(vec3(scale, scale, scale)) * position;	
}