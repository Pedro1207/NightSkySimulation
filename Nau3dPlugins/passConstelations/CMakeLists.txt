file(GLOB sources src/*.cpp)
file(GLOB headers src/*.h)

add_library(nauPassConstelations SHARED
	${sources} ${headers} "src/spa.cpp" "src/spa.h" )
	
add_dependencies(nauPassConstelations nau)
	
include_directories( 
	../../nau/src
	../../nau/src/nau
)	

if (WIN32)
	add_definitions(-D_WINDLL)
endif(WIN32)
	
target_link_libraries( nauPassConstelations $<TARGET_LINKER_FILE:nau> glbinding )


install (TARGETS nauPassConstelations DESTINATION lib)
install(FILES ${headers} DESTINATION include)	

add_custom_command(
	TARGET nauPassConstelations POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy
	$<TARGET_FILE:nauPassConstelations>
    ${CMAKE_BINARY_DIR}/bin/nauSettings/plugins/$<CONFIGURATION>/pass/$<TARGET_FILE_NAME:nauPassConstelations>)
					   
if (WIN32)
	set_target_properties (nauPassConstelations 
		PROPERTIES FOLDER plugins)	
endif(WIN32)		
