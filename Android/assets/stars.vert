#version 310 es

precision highp float;

uniform	mat4 m_pvm;
uniform	mat4 m_v;
uniform	mat4 m_p;

layout (location = 0) in vec4 position;	// local space
layout (location = 1) in vec4 instance;
layout (location = 2) in vec2 texCoord0;

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

mat4 scaleMatrix(vec3 scale)
{

    return transpose(mat4(scale.x,  0.0,      0.0,      0.0,
                          0.0,      scale.y,  0.0,      0.0,
                          0.0,      0.0,      scale.z,  0.0,
                          0.0,      0.0,      0.0,      1.0));
}

float processingMapClamped(float value, float min1, float max1, float min2, float max2) {
    float mapped = min2 + (value - min1) * (max2 - min2) / (max1 - min1);
    if (mapped < max2) mapped = 0.0;
    if (mapped > min2) mapped = 1.0;
    return mapped;
}

void main () {

    vec4 starInfo = instance;
    texCoord = texCoord0;
    magnitude = starInfo.w;

    mat4 m_vm = m_v * translationMatrix(starInfo.xyz);
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