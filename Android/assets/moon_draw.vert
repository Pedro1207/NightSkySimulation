#version 310 es
#extension GL_EXT_shader_io_blocks : enable

precision highp float;

const float PI = 3.14159265358979323846;

uniform	mat4 m_pvm;
uniform	mat4 m_viewModel;
uniform	mat4 m_view;
uniform	mat3 m_normal;

in vec4 position;
in vec3 normals;		// local space
in vec2 texCoord0;

uniform vec3 moonPosition;
uniform vec3 l_dir;
uniform vec3 moonData;

out Data {
    vec4 l_dir;
    vec3 normal;
    float rotation;
	vec2 texCoord;
} DataOut;



mat4 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    
    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
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

    DataOut.texCoord = texCoord0;

    DataOut.l_dir = vec4(l_dir, 0.0);
    float moonScale = 1.0;

    float distToMoon = moonData.y;
    float rotationOffset = ((distToMoon - 356793.0) / 49716.0) * radians(16.0);
    float rotation = -moonData.x + 3.5 + rotationOffset;
    
    
    DataOut.normal = normalize(m_normal * (rotationMatrix(vec3(0.0, 1.0, 0.0), rotation) * vec4(normals, 0.0)).xyz);

    gl_Position = m_pvm * translationMatrix(moonPosition.xyz) * rotationMatrix(vec3(0.0, 1.0, 0.0), rotation) * scaleMatrix(vec3(moonScale, moonScale, moonScale)) * position;
}
