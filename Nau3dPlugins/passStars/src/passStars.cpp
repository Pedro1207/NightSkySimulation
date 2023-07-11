#define PI         3.1415926535897932384626433832795028841971

#include "passStars.h"
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

using namespace std;

#ifdef WIN32
#include <Windows.h>
#endif

static char className[] = "starsPI";

#ifdef WIN32
#define EXPORT __declspec(dllexport)
#elif __linux__
#define EXPORT extern "C"
#endif

int usableStarCount;


EXPORT
void*
createPass(const char* s) {

	std::shared_ptr<PassStars>* p = new std::shared_ptr<PassStars>(new PassStars(s));
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
PassStars::Create(const std::string& passName) {

	return new PassStars(passName);
}


PassStars::PassStars(const std::string& passName) :
	Pass(passName) {

	m_ClassName = "starsPI";
	m_Inited = false;
}


PassStars::~PassStars(void) {

}

void PassStars::tokenize(string s, string delimiter, vector<string>& out) {
	string str = s;
	size_t current;
	size_t next = -1;
	do
	{
		current = next + 1;
		next = str.find_first_of(delimiter, current);
		out.push_back(s.substr(current, next - current));
	} while (next != string::npos);
}

void PassStars::readStarFile()
{
	ifstream fileStream;
	fileStream.open("hyg.csv");
	string file;
	string line;
	vector<string> tokens;

	int size = 0;
	largestMag = -100000;
	smallestMag = 1000000;

	if (fileStream.is_open()) {

		std::cout << "Reading star file" << endl;

		stringstream buffer;
		buffer << fileStream.rdbuf();
		int hasConst = 0;

		getline(buffer, line);
		while (getline(buffer, line)) {
			tokenize(line, ";", tokens);
			try {
				hip.push_back(stoi(tokens[1]));
			}
			catch (const std::invalid_argument& ia) {
				hip.push_back(-1);
			}
			proper.push_back(tokens[2]);
			ra.push_back(stof(tokens[3]));
			dec.push_back(stof(tokens[4]));
			mag.push_back(stof(tokens[5]));
			absmag.push_back(stof(tokens[6]));
			con.push_back(tokens[7]);

			if (stof(tokens[5]) < smallestMag) {
				smallestMag = stof(tokens[5]);
			}
			if (stof(tokens[5]) > largestMag) {
				largestMag = stof(tokens[5]);
			}

			tokens.clear();
		}

		std::cout << "Read " << ra.size() << " stars " << endl;
	}
	else {
		std::cout << "Error reading star information file." << endl;
	}


}

void convert_AZ_EL_to_XYZ(double azimuth, double elevation, double* x, double* y, double* z) {
	double distance = 400;
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

float getFloat(string name) {
	int ID = RENDERER->getAttribSet()->getID(name);
	float value = *((float*)RENDERER->getProp(ID, nau::Enums::DataType::FLOAT));
	return value;
}

void
PassStars::prepareStars() {

	int starCount = ra.size();

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
	double x, y, z;

	float normalizedMag;

	float* positions = (float*)malloc(sizeof(float) * starCount * 4);

	double tra, tdec;


	calculate_geocentric_sun_right_ascension_and_declination(&spa);

	int constelation = getInt("constelation");
	usableStarCount = 0;
	string constNames[] = {"None", "And", "Ant", "Aps", "Aql", "Aqr", "Ara", "Ari", "Aur", "Boo", "Cae", "Cam", "Cap", "Car", "Cas", "Cen", "Cep", "Cet", "Cha", "Cir", "CMa", "CMi", "Cnc", "Col", "Com", "CrA", "CrB", "Crt", "Cru", "Crv", "CVn", "Cyg", "Del", "Dor", "Dra", "Equ", "Eri", "For", "gem", "Gru", "Her", "Hor", "Hya", "Hyi", "Lnd", "Lac", "Leo", "Lep", "Lib", "LMi", "Lup", "Lyn", "Lyr", "Men", "Mic", "Mon", "Mus", "Nor", "Oct", "Oph", "Ori", "Pav", "Peg", "Per", "Phe", "Pic", "PsA", "Psc", "PuP", "Pyx", "Ret", "Scl", "Sco", "Sct", "Ser", "Sex", "Sge", "Sgr", "Tau", "Tel", "TrA", "Tri", "Tuc", "UMa", "UMi", "Vel", "Vir", "Vol", "Vul"};
	string name = constNames[constelation];

	int current = 0;
	for (int i = 0; i < starCount; i++) {

		if (name == "None" || con[i] == name) {
			tra = ra[i] * (360.0 / 24);
			spa.alpha = tra;
			spa.delta = dec[i];
			spa_calculate(&spa);



			azimuth = (-spa.azimuth_astro + 180) / 180 * PI;
			elevation = (spa.e - 90) / 180 * PI;

			convert_AZ_EL_to_XYZ(azimuth, elevation, &x, &y, &z);

			positions[current * 4] = (float)x;
			positions[current * 4 + 1] = (float)y;
			positions[current * 4 + 2] = (float)z;
			positions[current * 4 + 3] = mag[i];

			current++;
		}
	}

	usableStarCount = current;

	string buffer = "atmos::stars";
	RESOURCEMANAGER->getBuffer(buffer)->setData(starCount * 4 * sizeof(float), positions);

	m_Inited = true;

	cout << "Finished" << endl;

}


void
PassStars::prepare(void) {

	if (!m_Inited) {
		readStarFile();
		prepareStars();
		setInt("tChanged", 0);
	}

	if (getInt("tChanged")) {
		prepareStars();
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

	RENDERER->setPropui(IRenderer::INSTANCE_COUNT, usableStarCount);
	RENDERER->setPropui(IRenderer::BUFFER_DRAW_INDIRECT, m_UIntProps[BUFFER_DRAW_INDIRECT]);

}


void
PassStars::restore(void) {

	if (0 != m_RenderTarget && true == m_UseRT) {
		m_RenderTarget->unbind();
	}

}


void
PassStars::doPass(void) {

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

