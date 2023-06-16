#version 450

uniform	mat4 m_pvm;
uniform	mat4 m_viewModel;
uniform	mat4 m_view;
uniform	mat3 m_normal;

in vec4 position;
in vec3 normal;		// local space
in vec2 texCoord0;

uniform sampler2D mpa_data;

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

    DataOut.texCoord = texCoord0;   

    vec4 moonPosition = texelFetch(mpa_data, ivec2(0, 0), 0);
    vec4 l_dir = m_view * texelFetch(mpa_data, ivec2(1, 0), 0);
    vec4 moonData = texelFetch(mpa_data, ivec2(2, 0), 0);

    DataOut.l_dir = l_dir;
    float moonScale = 1;

    float distToMoon = moonData.y;
    float rotationOffset = ((distToMoon - 356793) / 49716) * radians(16);
    float rotation = moonData.x + 3.5 + rotationOffset;
    
    
    DataOut.normal = normalize(m_normal * (rotationMatrix(vec3(0.0, 1.0, 0.0), rotation) * vec4(normal, 0.0)).xyz);

    gl_Position = m_pvm * translationMatrix(moonPosition.xyz) * rotationMatrix(vec3(0.0, 1.0, 0.0), rotation) * scaleMatrix(vec3(moonScale, moonScale, moonScale)) * position;
}
