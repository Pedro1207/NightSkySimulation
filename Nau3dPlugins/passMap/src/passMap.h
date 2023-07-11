#ifndef PASSMAP_H
#define PASSMAP_H


#include "iNau.h"
#include "nau/render/pass.h"
#include "nau/scene/scene.h"

class PassMap : public Pass
		{
		protected:

			bool m_Inited;

		public:

			static Pass *Create(const std::string &passName);
			PassMap(const std::string &name);
			~PassMap(void);

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

#endif //PASSMAP_H
