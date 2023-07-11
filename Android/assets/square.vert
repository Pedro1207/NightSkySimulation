#version 310 es

precision highp float;

uniform mat4 uMVPMatrix;
in vec4 vPosition;

out vec2 texCoord;

void main() {
  gl_Position = uMVPMatrix * vPosition;
  texCoord = (vPosition.xy + 1.0) / 2.0;
}