#ifndef PASSSTARS_H
#define PASSSTARS_H


#include "iNau.h"
#include "nau/render/pass.h"
#include "nau/scene/scene.h"
#include <string>
#include <vector>

class PassStars : public Pass
		{
		protected:

			bool m_Inited;
			vector<int> hip;
			vector<string> proper;
    		vector<float> ra;
    		vector<float> dec;
    		vector<float> mag;
    		vector<float> absmag;
    		vector<string> con;

			float largestMag;
			float smallestMag;

			void prepareStars();
			void tokenize(string s, string delimiter, vector<string>& out);
			void readStarFile();

		public:

			static Pass *Create(const std::string &passName);
			PassStars(const std::string &name);
			~PassStars(void);

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

#endif //PASSSTARS_H
