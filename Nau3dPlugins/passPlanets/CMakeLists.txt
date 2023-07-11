file(GLOB sources src/*.cpp)
file(GLOB headers src/*.h)

add_library(nauPassPlanets SHARED
	${sources} ${headers})
	
add_dependencies(nauPassPlanets nau)
	
include_directories( 
	../../nau/src
	../../nau/src/nau
)	

if (WIN32)
	add_definitions(-D_WINDLL)
endif(WIN32)
	
target_link_libraries( nauPassPlanets $<TARGET_LINKER_FILE:nau> glbinding )


install (TARGETS nauPassPlanets DESTINATION lib)
install(FILES ${headers} DESTINATION include)	

add_custom_command(
	TARGET nauPassPlanets POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy
	$<TARGET_FILE:nauPassPlanets>
    ${CMAKE_BINARY_DIR}/bin/nauSettings/plugins/$<CONFIGURATION>/pass/$<TARGET_FILE_NAME:nauPassPlanets>)
					   
if (WIN32)
	set_target_properties (nauPassPlanets 
		PROPERTIES FOLDER plugins)	
endif(WIN32)		
