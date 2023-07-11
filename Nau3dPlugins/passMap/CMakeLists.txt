file(GLOB sources src/*.cpp)
file(GLOB headers src/*.h)

add_library(nauPassMap SHARED
	${sources} ${headers})
	
add_dependencies(nauPassMap nau)
	
include_directories( 
	../../nau/src
	../../nau/src/nau
)	

if (WIN32)
	add_definitions(-D_WINDLL)
endif(WIN32)
	
target_link_libraries( nauPassMap $<TARGET_LINKER_FILE:nau> glbinding )


install (TARGETS nauPassMap DESTINATION lib)
install(FILES ${headers} DESTINATION include)	

add_custom_command(
	TARGET nauPassMap POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy
	$<TARGET_FILE:nauPassMap>
    ${CMAKE_BINARY_DIR}/bin/nauSettings/plugins/$<CONFIGURATION>/pass/$<TARGET_FILE_NAME:nauPassMap>)
					   
if (WIN32)
	set_target_properties (nauPassMap 
		PROPERTIES FOLDER plugins)	
endif(WIN32)		
