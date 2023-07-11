#ifndef PASSCONSTELATIONS_H
#define PASSCONSTELATIONS_H


#include "iNau.h"
#include "nau/render/pass.h"
#include "nau/scene/scene.h"

class PassConstelations : public Pass
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
			map<int, int> starMap;
			map<string, vector<int>*> constMap;

			float largestMag;
			float smallestMag;

			
			void tokenize(string s, string delimiter, vector<string>& out);
			void readStarFile();
			void readConstFile();

			void prepareGeometry();
			void updateGeometry();
			

		public:

			static Pass *Create(const std::string &passName);
			PassConstelations(const std::string &name);
			~PassConstelations(void);

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

#endif //PASSCONSTELATIONS_H
