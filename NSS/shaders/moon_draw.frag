#version 330

uniform sampler2D moonTex;

uniform vec4 camera_view;

#define PI 3.14159265

float orenNayarDiffuse(
  vec3 lightDirection,
  vec3 viewDirection,
  vec3 surfaceNormal,
  float roughness,
  float albedo) {
  
  float LdotV = dot(lightDirection, viewDirection);
  float NdotL = dot(lightDirection, surfaceNormal);
  float NdotV = dot(surfaceNormal, viewDirection);

  float s = LdotV - NdotL * NdotV;
  float t = mix(1.0, max(NdotL, NdotV), step(0.0, s));

  float sigma2 = roughness * roughness;
  float A = 1.0 + sigma2 * (albedo / (sigma2 + 0.13) + 0.5 / (sigma2 + 0.33));
  float B = 0.45 * sigma2 / (sigma2 + 0.09);

  return albedo * max(0.0, NdotL) * (A + B * s / t) / PI;
}


in Data {
    vec4 l_dir;
    vec3 normal;
    float rotation;
	vec2 texCoord;
} DataIn;

out vec4 colorOut;

void main() {
	
	vec3 normal = normalize(DataIn.normal);
  vec3 l = normalize(DataIn.l_dir.xyz);
	vec3 view_vector = -normalize(camera_view.xyz);

	float diffuse_reflexion = orenNayarDiffuse(l, view_vector, normal, 0.1, 0.3);
	float intensity = 3;
	//float intensity = max(dot(normal,l), 0.0);

	vec4 diffuse = texture(moonTex, DataIn.texCoord);
	colorOut = intensity * diffuse_reflexion * diffuse;

}