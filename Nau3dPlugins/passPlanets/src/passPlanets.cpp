#define PI         3.1415926535897932384626433832795028841971

#include "passPlanets.h"
#include "spa.h"

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

#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <sstream>

#define PI          3.14159265358979323846
#define RADEG       (180.0/PI)
#define DEGRAD      (PI/180.0)
#define sind(x)     sin((x)*DEGRAD)
#define cosd(x)     cos((x)*DEGRAD)
#define tand(x)     tan((x)*DEGRAD)
#define asind(x)    (RADEG*asin(x))
#define acosd(x)    (RADEG*acos(x))
#define atand(x)    (RADEG*atan(x))
#define atan2d(y,x) (RADEG*atan2((y),(x)))

typedef struct elements {
	double N;
	double i;
	double w;
	double a;
	double e;
	double M;
	double RA;
	double DEC;
	double r;
}*Elements;

using namespace std;

#ifdef WIN32
#include <Windows.h>
#endif

static char className[] = "planetsPI";

#ifdef WIN32
#define EXPORT __declspec(dllexport)
#elif __linux__
#define EXPORT extern "C"
#endif



EXPORT
void*
createPass(const char* s) {

	std::shared_ptr<PassPlanets>* p = new std::shared_ptr<PassPlanets>(new PassPlanets(s));
	return p;
}


EXPORT
void
init(void* nauInst) {

	INau::SetInterface((nau::INau*)nauInst);
	nau::Nau::SetInstance((nau::Nau*)nauInst);
	glbinding::Binding::initialize(false);
}


EXPORT
char*
getClassName() {

	return className;
}


using namespace nau::geometry;
using namespace nau::math;
using namespace nau::render;
using namespace nau::scene;

Pass*
PassPlanets::Create(const std::string& passName) {

	return new PassPlanets(passName);
}


PassPlanets::PassPlanets(const std::string& passName) :
	Pass(passName) {

	m_ClassName = "planetsPI";
	m_Inited = false;
}


PassPlanets::~PassPlanets(void) {

}


void convert_AZ_EL_to_XYZ(double azimuth, double elevation, double* x, double* y, double* z, float r) {
	double distance = (double) r;
	*x = distance * sin(azimuth) * sin(elevation);
	*y = distance * cos(elevation);
	*z = distance * cos(azimuth) * sin(elevation);
}


float processingMap(float value, float min1, float max1, float min2, float max2) {
	return min2 + (value - min1) * (max2 - min2) / (max1 - min1);
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

void setFloat(string name, float value) {
	int ID = RENDERER->getAttribSet()->getID(name);
	RENDERER->setPropf((AttributeValues::FloatProperty)ID, value);
}

float getFloat(string name) {
	int ID = RENDERER->getAttribSet()->getID(name);
	float value = *((float*)RENDERER->getProp(ID, nau::Enums::DataType::FLOAT));
	return value;
}

double rev(double x)
{
	return  x - floor(x / 360.0) * 360.0;
}

double cbrt(double x)
{
	if (x > 0.0)
		return exp(log(x) / 3.0);
	else if (x < 0.0)
		return -cbrt(-x);
	else /* x == 0.0 */
		return 0.0;
}


float compute_day(int year, int month, int day, float hour) {
	int id = 367 * year - 7 * (year + (month + 9) / 12) / 4 - 3 * ((year + (month - 9) / 7) / 100 + 1) / 4 + 275 * month / 9 + day - 730515;
	float d = id + hour / 24;
	return d;
}

void computePositionSun(Elements elements, float d, double* xs, double* ys) {
	double E = elements->M + elements->e * (180 / PI) * sind(elements->M) * (1.0 + elements->e * cosd(elements->M));

	double xv = cosd(E) - elements->e;
	double yv = sqrt(1.0 - elements->e * elements->e) * sind(E);

	double v = atan2d(yv, xv);
	double r = sqrt(xv * xv + yv * yv);

	double lonsun = v + elements->w;

	*xs = r * cosd(lonsun);
	*ys = r * sind(lonsun);
}

void computePositionPlanet(Elements elements, float d, double xs, double ys, double *distance, double *heliox, double* helioy, double* helioz) {

	double ecl = 23.4393 - 3.563E-7 * d;
	double E;
	if (elements->e < 0.06) {
		E = elements->M + elements->e * (RADEG)*sind(elements->M) * (1.0 + elements->e * cosd(elements->M));
	}
	else {
		int n = 0;
		double E0 = 0.0;
		double E1;
		while (n < 100) {
			E1 = E0 - (E0 - elements->e * RADEG * sind(E0) - elements->M) / (1 - elements->e * cosd(E0));
			if (abs(E1 - E0) < 0.001) {
				break;
			}
			else {
				E0 = E1;
			}
			n++;
		}
		E = E1;
		if (n == 100) std::cout << "Didn't converge" << std::endl;
	}

	double xv = elements->a * (cosd(E) - elements->e);
	double yv = elements->a * (sqrt(1.0 - elements->e * elements->e) * sind(E));

	double v = atan2d(yv, xv);
	double r = sqrt(xv * xv + yv * yv);

	double xh = r * (cosd(elements->N) * cosd(v + elements->w) - sind(elements->N) * sind(v + elements->w) * cosd(elements->i));
	double yh = r * (sind(elements->N) * cosd(v + elements->w) + cosd(elements->N) * sind(v + elements->w) * cosd(elements->i));
	double zh = r * (sind(v + elements->w) * sind(elements->i));
	*heliox = xh;
	*helioy = yh;
	*helioz = zh;

	double lonecl = atan2d(yh, xh);
	double latecl = atan2d(zh, sqrt(xh * xh + yh * yh));

	double lon_corr = 3.82394E-5 * -d;

	lonecl += lon_corr;

	xh = r * cosd(lonecl) * cosd(latecl);
	yh = r * sind(lonecl) * cosd(latecl);
	zh = r * sind(latecl);

	double xg = xh + xs;
	double yg = yh + ys;
	double zg = zh;

	double xe = xg;
	double ye = yg * cosd(ecl) - zg * sind(ecl);
	double ze = yg * sind(ecl) + zg * cosd(ecl);

	double RA = atan2d(ye, xe);
	double Dec = atan2d(ze, sqrt(xe * xe + ye * ye + ze * ze));
	double rg = sqrt(xe * xe + ye * ye + ze * ze);
	*distance = rg;

	elements->RA = RA;
	elements->DEC = Dec;
	elements->r = rg;

}

void
PassPlanets::preparePlanets() {


	float intPart;

	double latitude = getFloat("latitude");
	double longitude = getFloat("longitude"); //maybe negative

	float decimalHour = getFloat("decimalHour");
	int hour = floor(decimalHour);
	int minutes = floor(modf(decimalHour, &intPart) * 60);
	float seconds = modf(modf(decimalHour, &intPart) * 60, &intPart) * 60;

	spa_data spa;  //declare the SPA structure

	//enter required input values into SPA structure

	spa.year = getInt("year");
	spa.month = getInt("month");
	spa.day = getInt("day");
	spa.hour = hour;
	spa.minute = minutes;
	spa.second = seconds;
	spa.timezone = 0;
	spa.delta_ut1 = 0;
	spa.delta_t = 67;
	spa.longitude = longitude;
	spa.latitude = latitude;
	spa.elevation = 200;
	spa.pressure = 820;
	spa.temperature = 11;
	spa.slope = 0;
	spa.azm_rotation = -10;
	spa.atmos_refract = 0.5667;

	double azimuth, elevation;
	double x, y, z, realX, realY, realZ;

	float normalizedMag;

	float* positions = (float*)malloc(sizeof(float) * 7 * 4 * 2);
	double tra, tdec;


	float d = compute_day(spa.year, spa.month, spa.day, decimalHour);

	Elements sunElements = (Elements)malloc(sizeof(struct elements));
	sunElements->N = 0.0;
	sunElements->i = 0.0;
	sunElements->w = 282.9404 + 4.70935E-5 * d;
	sunElements->a = 1.000000;
	sunElements->e = 0.016709 - 1.151E-9 * d;
	sunElements->M = 356.0470 + 0.9856002585 * d;

	Elements mercuryElements = (Elements)malloc(sizeof(struct elements));
	mercuryElements->N = 48.3313 + 3.24587E-5 * d;
	mercuryElements->i = 7.0047 + 5.00E-8 * d;
	mercuryElements->w = 29.1241 + 1.01444E-5 * d;
	mercuryElements->a = 0.387098;
	mercuryElements->e = 0.205635 + 5.59E-10 * d;
	mercuryElements->M = 168.6562 + 4.0923344368 * d;

	Elements venusElements = (Elements)malloc(sizeof(struct elements));
	venusElements->N = 76.6799 + 2.46590E-5 * d;
	venusElements->i = 3.3946 + 2.75E-8 * d;
	venusElements->w = 54.8910 + 1.38374E-5 * d;
	venusElements->a = 0.723330;
	venusElements->e = 0.006773 - 1.302E-9 * d;
	venusElements->M = 48.0052 + 1.6021302244 * d;

	Elements marsElements = (Elements)malloc(sizeof(struct elements));
	marsElements->N = 49.5574 + 2.11081E-5 * d;
	marsElements->i = 1.8497 - 1.78E-8 * d;
	marsElements->w = 286.5016 + 2.92961E-5 * d;
	marsElements->a = 1.523688;
	marsElements->e = 0.093405 + 2.516E-9 * d;
	marsElements->M = 18.6021 + 0.5240207766 * d;

	Elements jupiterElements = (Elements)malloc(sizeof(struct elements));
	jupiterElements->N = 100.4542 + 2.76854E-5 * d;
	jupiterElements->i = 1.3030 - 1.557E-7 * d;
	jupiterElements->w = 273.8777 + 1.64505E-5 * d;
	jupiterElements->a = 5.20256;
	jupiterElements->e = 0.048498 + 4.469E-9 * d;
	jupiterElements->M = 19.8950 + 0.0830853001 * d;

	Elements saturnElements = (Elements)malloc(sizeof(struct elements));
	saturnElements->N = 113.6634 + 2.38980E-5 * d;
	saturnElements->i = 2.4886 - 1.081E-7 * d;
	saturnElements->w = 339.3939 + 2.97661E-5 * d;
	saturnElements->a = 9.55475;
	saturnElements->e = 0.055546 - 9.499E-9 * d;
	saturnElements->M = 316.9670 + 0.0334442282 * d;

	Elements uranusElements = (Elements)malloc(sizeof(struct elements)); //few centuries of accuracy
	uranusElements->N = 74.0005 + 1.3978E-5 * d;
	uranusElements->i = 0.7733 + 1.9E-8 * d;
	uranusElements->w = 96.6612 + 3.0565E-5 * d;
	uranusElements->a = 19.18171 - 1.55E-8 * d;
	uranusElements->e = 0.047318 + 7.45E-9 * d;
	uranusElements->M = 142.5905 + 0.011725806 * d;

	Elements neptuneElements = (Elements)malloc(sizeof(struct elements)); //few centuries of accuracy
	neptuneElements->N = 131.7806 + 3.0173E-5 * d;
	neptuneElements->i = 1.7700 - 2.55E-7 * d;
	neptuneElements->w = 272.8461 - 6.027E-6 * d;
	neptuneElements->a = 30.05826 + 3.313E-8 * d;
	neptuneElements->e = 0.008606 + 2.15E-9 * d;
	neptuneElements->M = 260.2471 + 0.005995147 * d;

	std::vector<Elements> elementsVector;
	elementsVector.push_back(mercuryElements);
	elementsVector.push_back(venusElements);
	elementsVector.push_back(marsElements);
	elementsVector.push_back(jupiterElements);
	elementsVector.push_back(saturnElements);
	elementsVector.push_back(uranusElements);
	elementsVector.push_back(neptuneElements);


	double xs, ys;

	computePositionSun(sunElements, d, &xs, &ys);

	double distance, xh, yh, zh;
	std:vector<float> distances;
	for (Elements e : elementsVector) {
		computePositionPlanet(e, d, xs, ys, &distance, &xh, &yh, &zh);
		distances.push_back((float)distance);
		distances.push_back((float)xh);
		distances.push_back((float)yh);
		distances.push_back((float)zh);
		cout << "planets: " << xh << " - " << yh << " - " << zh << endl;
	}


	calculate_geocentric_sun_right_ascension_and_declination(&spa);

	int AU = 149598000;
	std::vector<float> radii;
	radii.push_back(2439.7);
	radii.push_back(6051.8);
	radii.push_back(3389.5);
	radii.push_back(69911);
	radii.push_back(58232);
	radii.push_back(25362);
	radii.push_back(24622);


	for (int i = 0; i < 7; i++) {
		tra = elementsVector[i]->RA;
		spa.alpha = tra;
		spa.delta = elementsVector[i]->DEC;
		spa_calculate(&spa);

		azimuth = (-spa.azimuth_astro + 180) / 180 * PI;
		elevation = (spa.e - 90) / 180 * PI;


		convert_AZ_EL_to_XYZ(azimuth, elevation, &x, &y, &z, distances[i*4] * 10);

		positions[i*4] = (float) x;
		positions[i*4+1] = (float) y;
		positions[i*4+2] = (float) z;
		positions[i*4+3] = radii[i] / AU * 10;
		positions[7*4 + i * 4] = distances[i * 4 + 1];
		positions[7*4 + i * 4 + 1] = distances[i * 4 + 2];
		positions[7*4 + i * 4 + 2] = distances[i * 4 + 3];
		positions[7*4 + i * 4 + 3] = 0;
	}


	string buffer = "atmos::planets";
	RESOURCEMANAGER->getBuffer(buffer)->setData(7 * 4 * 2 * sizeof(float), positions);

	m_Inited = true;

	for (Elements e : elementsVector) {
		free(e);
	}

}


void
PassPlanets::prepare(void) {

	if (!m_Inited) {
		preparePlanets();
		setInt("tChanged", 0);
	}

	if (getInt("tChanged")) {
		preparePlanets();
	}


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

	RENDERER->setPropui(IRenderer::INSTANCE_COUNT, m_UIntProps[INSTANCE_COUNT]);
	RENDERER->setPropui(IRenderer::BUFFER_DRAW_INDIRECT, m_UIntProps[BUFFER_DRAW_INDIRECT]);

}


void
PassPlanets::restore(void) {

	if (0 != m_RenderTarget && true == m_UseRT) {
		m_RenderTarget->unbind();
	}

}


void
PassPlanets::doPass(void) {

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

