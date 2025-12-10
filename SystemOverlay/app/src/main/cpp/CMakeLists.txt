# CMake configuration for Native C++ Module
# Optimized for Android TV performance monitoring

cmake_minimum_required(VERSION 3.10.2)

project("systemoverlay_native")

# C++17 for modern features and performance
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Optimization flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math -fno-exceptions -fno-rtti")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -Werror")

# Create shared library
add_library(
    systemoverlay_native
    SHARED
    native_proc_reader.cpp
)

# Link against Android logging library
find_library(
    log-lib
    log
)

target_link_libraries(
    systemoverlay_native
    ${log-lib}
)

# Strip debug symbols in release builds
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_custom_command(
        TARGET systemoverlay_native
        POST_BUILD
        COMMAND ${CMAKE_STRIP} --strip-unneeded $<TARGET_FILE:systemoverlay_native>
    )
endif()
