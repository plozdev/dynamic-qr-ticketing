cmake_minimum_required(VERSION 3.22.1)

project("dynamic_qr_crypto")

# Support C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Include directories
include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/include
)

# Source files
set(CRYPTO_SOURCES
    src/crypto_engine.cpp
    src/secure_storage.cpp
    src/jni_bridge.cpp
)

# Compile dynamic library: libdynamic_qr_crypto.so
add_library(
    dynamic_qr_crypto
    SHARED
    ${CRYPTO_SOURCES}
)

# Find Android system libraries (Logcat logging)
find_library(
    log-lib
    log
)

# Link dependencies
target_link_libraries(
    dynamic_qr_crypto
    ${log-lib}
)
