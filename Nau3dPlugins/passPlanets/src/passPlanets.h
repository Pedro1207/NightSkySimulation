#ifndef PASSPLANETS_H
#define PASSPLANETS_H


#include "iNau.h"
#include "nau/render/pass.h"
#include "nau/scene/scene.h"
#include <string>
#include <vector>

class PassPlanets : public Pass
		{
		protected:

			bool m_Inited;
			vector<string> proper;
    		vector<float> ra;
    		vector<float> dec;
    		vector<float> mag;
    		vector<float> absmag;
    		vector<string> con;

			float largestMag;
			float smallestMag;

			void preparePlanets();
			void tokenize(string& s, string delimiter, vector<string>& out);
			void readStarFile();
			void calculatePosition(float ra, float dec, float julian, float latitude, float longitude, float *azimuth, float *zenith);

		public:

			static Pass *Create(const std::string &passName);
			PassPlanets(const std::string &name);
			~PassPlanets(void);

			virtual void prepare (void);
			virtual void doPass (void);
			virtual void restore (void);


};

extern "C" {
#ifdef WIN32
	__declspec(dllexport) void *createPass(const char *s);
	__declspec(dllexport) void init(void *inau);
	__declspec(dllexport) char *getClassName();
#else
	void *createPass(const char *s);
	void init(void *inau);
	char *getClassName();
#endif	
}

#endif //PASSPLANETS_H
