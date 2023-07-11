#include "passConstelations.h"
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

#include <map>

#ifdef WIN32
#include <Windows.h>
#endif

#define PI         3.1415926535897932384626433832795028841971

static char className[] = "constelationsPI";

#ifdef WIN32
#define EXPORT __declspec(dllexport)
#elif __linux__
#define EXPORT extern "C"
#endif

EXPORT 
void *
createPass(const char *s) {

	std::shared_ptr<PassConstelations> *p = new std::shared_ptr<PassConstelations>(new PassConstelations(s));
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
PassConstelations::Create(const std::string &passName) {

	return new PassConstelations(passName);
}


PassConstelations::PassConstelations(const std::string &passName) :
	Pass (passName) {

	m_ClassName = "constelationsPI";
	m_Inited = false;
}


PassConstelations::~PassConstelations(void) {

}


void PassConstelations::tokenize(string s, string delimiter, vector<string>& out) {
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

void PassConstelations::readStarFile()
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

		getline(buffer, line);
		int invalidHip = -1;
		while (getline(buffer, line)) {
			tokenize(line, ";", tokens);
			try {
				hip.push_back(stoi(tokens[1]));
				starMap.insert({ stoi(tokens[1]), stoi(tokens[0])});
			}
			catch (const std::invalid_argument& ia) {
				hip.push_back(-1);
				starMap.insert({ invalidHip--, stoi(tokens[0]) });
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

		std::cout << "Read " << ra.size() << "stars " << endl;
	}
	else {
		std::cout << "Error reading star information file." << endl;
	}


}



void PassConstelations::readConstFile()
{
	ifstream fileStream;
	fileStream.open("consts.txt");
	string file;
	string line;
	string name;
	vector<string> tokens;
	vector<int> *stars;
	bool first = true;

	int size = 0;

	if (fileStream.is_open()) {

		std::cout << "Reading const file" << endl;

		stringstream buffer;
		buffer << fileStream.rdbuf();

		while (getline(buffer, line)) {

			if (line[0] >= 'A' && line[0] <= 'Z') {
				if (!first) {
					constMap.insert({ name, stars });
				}
				name = line;
				name = name.substr(0, 3);
				stars = new vector<int>();
				first = false;
			}
			else if (line[0] >= '0' && line[0] <= '9'){
				tokenize(line, ",", tokens);
				stars->push_back(stoi(tokens[0]));
				stars->push_back(stoi(tokens[1]));
				stars->push_back(stoi(tokens[0]));
				tokens.clear();
			}

		}

		constMap.insert({ name, stars });
		constMap.insert({ "None", new vector<int>() });

	}
	else {
		std::cout << "Error reading const information file." << endl;
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
PassConstelations::prepareGeometry() {


	std::shared_ptr<IScene> m_Scene = RENDERMANAGER->createScene("ConstScene", "Scene");

	// create a renderable
	std::shared_ptr<nau::render::IRenderable> &m_Renderable = RESOURCEMANAGER->createRenderable("Mesh", "ConstPlane");

	int constelation = getInt("constelation");
	string constNames[] = { "None", "And", "Ant", "Aps", "Aql", "Aqr", "Ara", "Ari", "Aur", "Boo", "Cae", "Cam", "Cap", "Car", "Cas", "Cen", "Cep", "Cet", "Cha", "Cir", "CMa", "CMi", "Cnc", "Col", "Com", "CrA", "CrB", "Crt", "Cru", "Crv", "CVn", "Cyg", "Del", "Dor", "Dra", "Equ", "Eri", "For", "gem", "Gru", "Her", "Hor", "Hya", "Hyi", "Lnd", "Lac", "Leo", "Lep", "Lib", "LMi", "Lup", "Lyn", "Lyr", "Men", "Mic", "Mon", "Mus", "Nor", "Oct", "Oph", "Ori", "Pav", "Peg", "Per", "Phe", "Pic", "PsA", "Psc", "PuP", "Pyx", "Ret", "Scl", "Sco", "Sct", "Ser", "Sex", "Sge", "Sgr", "Tau", "Tel", "TrA", "Tri", "Tuc", "UMa", "UMi", "Vel", "Vir", "Vol", "Vul" };
	string name = constNames[constelation];

	// fill in vertex array
	int vertexCount = (*constMap[name]).size();

	std::shared_ptr<std::vector<VertexData::Attr>> vertices =
		std::shared_ptr<std::vector<VertexData::Attr>>(new std::vector<VertexData::Attr>(vertexCount));

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

	double tra, tdec;

	float maxEl = -10000;
	float minEl = 10000;

	calculate_geocentric_sun_right_ascension_and_declination(&spa);

	int index;
	for (int i = 0; i < vertexCount; i++) {
		index = starMap[(*constMap[name])[i]];
		tra = ra[index] * (360.0 / 24);
		spa.alpha = tra;
		spa.delta = dec[index];
		spa_calculate(&spa);

		azimuth = (-spa.azimuth_astro + 180) / 180 * PI;
		elevation = (spa.e - 90) / 180 * PI;


		convert_AZ_EL_to_XYZ(azimuth, elevation, &x, &y, &z);

		vertices->at(i).set(x, y, z);

	}


	std::shared_ptr<VertexData>& vertexData = m_Renderable->getVertexData();

	vertexData->setDataFor(VertexData::GetAttribIndex(std::string("position")), vertices);

	// create indices and fill the array
	int indexCount = vertexCount;
	std::shared_ptr<std::vector<unsigned int>> indices =
		std::shared_ptr<std::vector<unsigned int>>(new std::vector<unsigned int>(indexCount));


	for (int i = 0; i < vertexCount; i++) {
		indices->at(i) = i;
	}

	// create the material group
	std::shared_ptr<MaterialGroup> aMaterialGroup = MaterialGroup::Create(m_Renderable.get(), "__Light Grey");
	aMaterialGroup->setIndexList(indices);
	m_Renderable->addMaterialGroup(aMaterialGroup);

	std::shared_ptr<SceneObject> &m_SceneObject = nau::scene::SceneObjectFactory::Create("SimpleObject");

	m_SceneObject->setRenderable(m_Renderable);
	
	m_Scene->add(m_SceneObject);

	addScene("ConstScene");

	m_Inited = true;

}


void
PassConstelations::updateGeometry() {

	std::shared_ptr<IScene> m_Scene = RENDERMANAGER->getScene("ConstScene");

	std::shared_ptr<SceneObject>& m_SceneObject = m_Scene->getSceneObject(0);
	std::shared_ptr<nau::render::IRenderable>& m_Renderable = m_SceneObject->getRenderable();

	std::shared_ptr<MaterialGroup> aMaterialGroup = m_Renderable->getMaterialGroups()[0];

	int constelation = getInt("constelation");
	string constNames[] = { "None", "And", "Ant", "Aps", "Aql", "Aqr", "Ara", "Ari", "Aur", "Boo", "Cae", "Cam", "Cap", "Car", "Cas", "Cen", "Cep", "Cet", "Cha", "Cir", "CMa", "CMi", "Cnc", "Col", "Com", "CrA", "CrB", "Crt", "Cru", "Crv", "CVn", "Cyg", "Del", "Dor", "Dra", "Equ", "Eri", "For", "gem", "Gru", "Her", "Hor", "Hya", "Hyi", "Lnd", "Lac", "Leo", "Lep", "Lib", "LMi", "Lup", "Lyn", "Lyr", "Men", "Mic", "Mon", "Mus", "Nor", "Oct", "Oph", "Ori", "Pav", "Peg", "Per", "Phe", "Pic", "PsA", "Psc", "PuP", "Pyx", "Ret", "Scl", "Sco", "Sct", "Ser", "Sex", "Sge", "Sgr", "Tau", "Tel", "TrA", "Tri", "Tuc", "UMa", "UMi", "Vel", "Vir", "Vol", "Vul" };
	string name = constNames[constelation];

	// fill in vertex array
	int vertexCount = (*constMap[name]).size();

	std::shared_ptr<std::vector<VertexData::Attr>> vertices =
		std::shared_ptr<std::vector<VertexData::Attr>>(new std::vector<VertexData::Attr>(vertexCount));

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

	double tra, tdec;

	float maxEl = -10000;
	float minEl = 10000;

	calculate_geocentric_sun_right_ascension_and_declination(&spa);

	int index;
	for (int i = 0; i < vertexCount; i++) {
		index = starMap[(*constMap[name])[i]];
		tra = ra[index] * (360.0 / 24);
		spa.alpha = tra;
		spa.delta = dec[index];
		spa_calculate(&spa);

		azimuth = (-spa.azimuth_astro + 180) / 180 * PI;
		elevation = (spa.e - 90) / 180 * PI;


		convert_AZ_EL_to_XYZ(azimuth, elevation, &x, &y, &z);


		vertices->at(i).set(x, y, z);

	}


	std::shared_ptr<VertexData>& vertexData = m_Renderable->getVertexData();

	vertexData->setDataFor(VertexData::GetAttribIndex(std::string("position")), vertices);

	// create indices and fill the array
	int indexCount = vertexCount;
	std::shared_ptr<std::vector<unsigned int>> indices =
		std::shared_ptr<std::vector<unsigned int>>(new std::vector<unsigned int>(indexCount));


	for (int i = 0; i < vertexCount; i++) {
		indices->at(i) = i;
	}


	aMaterialGroup->setIndexList(indices);

	aMaterialGroup->resetCompilationFlag();


}


void
PassConstelations::prepare (void) {

	if (!m_Inited) {
		readStarFile();
		readConstFile();
		prepareGeometry();
	}

	if (getInt("tChanged")) {
		updateGeometry();
		setInt("tChanged", 0);
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

}


void
PassConstelations::restore (void) {

	if (0 != m_RenderTarget && true == m_UseRT) {
		m_RenderTarget->unbind();
	}

}


void 
PassConstelations::doPass (void) {

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
	RENDERER->setRenderMode(nau::render::IRenderer::TRenderMode::WIREFRAME_MODE);

	RENDERMANAGER->processQueue();

	RENDERER->setRenderMode(nau::render::IRenderer::TRenderMode::MATERIAL_MODE);

}

