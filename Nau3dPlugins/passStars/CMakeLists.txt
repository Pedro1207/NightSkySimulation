file(GLOB sources src/*.cpp)
file(GLOB headers src/*.h)

add_library(nauPassStars SHARED
	${sources} ${headers})
	
add_dependencies(nauPassStars nau)
	
include_directories( 
	../../nau/src
	../../nau/src/nau
)	

if (WIN32)
	add_definitions(-D_WINDLL)
endif(WIN32)
	
target_link_libraries( nauPassStars $<TARGET_LINKER_FILE:nau> glbinding )


install (TARGETS nauPassStars DESTINATION lib)
install(FILES ${headers} DESTINATION include)	

add_custom_command(
	TARGET nauPassStars POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy
	$<TARGET_FILE:nauPassStars>
    ${CMAKE_BINARY_DIR}/bin/nauSettings/plugins/$<CONFIGURATION>/pass/$<TARGET_FILE_NAME:nauPassStars>)
					   
if (WIN32)
	set_target_properties (nauPassStars 
		PROPERTIES FOLDER plugins)	
endif(WIN32)		
