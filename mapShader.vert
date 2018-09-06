#version 400 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 texPos;
layout(location = 2) in float surfType;

out vec3 mPos;
out vec3 mTexPos;
out float mSurfType;

uniform vec3 transPos;
uniform mat4 cam;
uniform mat4 mapTrans;
uniform sampler2D tex;

void main() {
	
	gl_Position = cam * mapTrans * vec4(pos.xyz, 1);
	mTexPos = texPos;
	mSurfType = surfType;
	mPos = pos;
	
}