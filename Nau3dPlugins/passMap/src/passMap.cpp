#include "passMap.h"

#include "iNau.h"
#include "nau.h"
#include "nau/geometry/vertexData.h"
#include "nau/material/iTexture.h"
#include "nau/material/materialGroup.h"
#include "nau/math/vec3.h"
#include "nau/render/passFactory.h"
#include "nau/scene/sceneFactory.h"



#include <glbinding/gl/gl.h>
#include <glbinding/Binding.h>

#ifdef WIN32
#include <Windows.h>
#endif

static char className[] = "mapPI";

#ifdef WIN32
#define EXPORT __declspec(dllexport)
#elif __linux__
#define EXPORT extern "C"
#endif

#define PI         3.1415926535897932384626433832795028841971

EXPORT 
void *
createPass(const char *s) {

	std::shared_ptr<PassMap> *p = new std::shared_ptr<PassMap>(new PassMap(s));
	return p;
}


EXPORT
void 
init(void *nauInst) {

	INau::SetInterface((nau::INau *)nauInst);
	nau::Nau::SetInstance((nau::Nau *)nauInst);
	glbinding::Binding::initialize(false);
}


EXPORT
char *
getClassName() {

	return className;
}


using namespace nau::geometry;
using namespace nau::math;
using namespace nau::render;
using namespace nau::scene;

Pass *
PassMap::Create(const std::string &passName) {

	return new PassMap(passName);
}


PassMap::PassMap(const std::string &passName) :
	Pass (passName) {

	m_ClassName = "mapPI";
	m_Inited = false;
}


PassMap::~PassMap(void) {

}


int getInt(string name) {
	int ID = RENDERER->getAttribSet()->getID(name);
	int value = *((int*)RENDERER->getProp(ID, nau::Enums::DataType::INT));
	return value;
}

void setInt(string name, int value) {
	int ID = RENDERER->getAttribSet()->getID(name);
	RENDERER->setPropi((AttributeValues::IntProperty)ID, value);
}

float getFloat(string name) {
	int ID = RENDERER->getAttribSet()->getID(name);
	float value = *((float*)RENDERER->getProp(ID, nau::Enums::DataType::FLOAT));
	return value;
}

nau::math::vec4 getVec4(string name) {
	int ID = RENDERER->getAttribSet()->getID(name);
	nau::math::vec4 value = *((nau::math::vec4*)RENDERER->getProp(ID, nau::Enums::DataType::VEC4));
	return value;
}

void setFloat(string name, float value) {
	int ID = RENDERER->getAttribSet()->getID(name);
	RENDERER->setPropf((AttributeValues::FloatProperty)ID, value);
}



void
PassMap::prepare (void) {

	Camera* c = NAU->getActiveCamera();
	float zx_angle = c->getPropf(nau::scene::Camera::ZX_ANGLE);
	float elevation = c->getPropf(nau::scene::Camera::ELEVATION_ANGLE);


	nau::math::vec3 pos = nau::math::Spherical::toCartesian(zx_angle, -elevation);
	float r = 2.5;
	c->setPropf4(nau::scene::Camera::POSITION, pos.x * r, pos.y * r, pos.z * r, 1.0);
	c->setPropf4(nau::scene::Camera::VIEW_VEC, -pos.x * r, -pos.y * r, -pos.z * r, 0.0);

	while (zx_angle < 0) {
		zx_angle += (2 * PI);
	}

	while (zx_angle > 2 * PI) {
		zx_angle -= (2 * PI);
	}

	setFloat("longitude", (zx_angle - PI) * 180 / PI);
	setFloat("latitude", elevation * 180 / PI * -1);

	

	if (0 != m_RenderTarget && true == m_UseRT) {

		if (m_ExplicitViewport) {
			vec2 f2 = m_Viewport[0]->getPropf2(Viewport::ABSOLUTE_SIZE);
			m_RTSizeWidth = (int)f2.x;
			m_RTSizeHeight = (int)f2.y;
			uivec2 uiv2((unsigned int)m_RTSizeWidth, (unsigned int)m_RTSizeHeight);
			m_RenderTarget->setPropui2(IRenderTarget::SIZE, uiv2);
		}
		m_RenderTarget->bind();
	}

	prepareBuffers();
	setupCamera();

}


void
PassMap::restore (void) {

	if (0 != m_RenderTarget && true == m_UseRT) {
		m_RenderTarget->unbind();
	}

}


void 
PassMap::doPass (void) {

	RENDERMANAGER->clearQueue();

	std::vector<std::string>::iterator scenesIter;
	scenesIter = m_SceneVector.begin();

	for (; scenesIter != m_SceneVector.end(); ++scenesIter) {
		std::shared_ptr<IScene>& aScene = RENDERMANAGER->getScene(*scenesIter);
		std::vector<std::shared_ptr<SceneObject>> sceneObjects;
		aScene->getAllObjects(&sceneObjects);
		std::vector<SceneObject*>::iterator objIter;

		for (auto& so : sceneObjects) {

			RENDERMANAGER->addToQueue(so, m_MaterialMap);
		}
	}

	RENDERER->setDepthClamping(true);

	RENDERMANAGER->processQueue();

}

