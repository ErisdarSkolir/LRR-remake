#version 400 core

out vec3 mPosition;
out vec3 mColor;
out vec2 mTexPos;

layout(location = 0) in vec3 position;
layout(location = 1) in vec3 color;
layout(location = 2) in vec3 texPos;

uniform mat4 modelTrans;
uniform mat4 animTrans;
uniform mat4 cam;
uniform vec3 aColor;
uniform sampler2D tex;
uniform bool calcAlpha;
uniform float framealpha;

void main(void)
{
	gl_Position = cam * modelTrans * animTrans * vec4(position.xyz, 1.0);
	mColor = color;
	mPosition = position;
	mTexPos = texPos.xy;
}