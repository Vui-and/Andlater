cmake_minimum_required(VERSION 3.22.1)
project(customos_hal)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# This target is intentionally a thin supervisor, NOT a reimplementation of
# QEMU/Box86/Box64. Those are built as standalone Android-targeted binaries
# by .github/workflows/build.yml (real upstream sources: qemu/qemu,
# ptitSeb/box86, ptitSeb/box64) and shipped as prebuilt executables under
# jniLibs/<abi>/. This library's job is:
#   1. fork()+exec() the prebuilt qemu-system-* binary with the argv Kotlin
#      built (see QemuLauncher.kt / EmulatorBridge.kt),
#   2. supervise its lifecycle (start/stop/kill) from JNI calls,
#   3. bridge the Android Surface to whatever the emulator core renders to
#      (e.g. via QEMU's egl-headless display + a shared DMA-BUF, or a
#      simple pixel-copy fallback for a first working version).
add_library(customos_hal SHARED
    jni/emulator_bridge_jni.cpp
    hal/vm_supervisor.cpp
    hal/input_injector.cpp
    hal/surface_bridge.cpp
)

target_include_directories(customos_hal PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}/hal
    ${CMAKE_CURRENT_SOURCE_DIR}/jni
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(customos_hal
    ${log-lib}
    ${android-lib}
)

target_compile_definitions(customos_hal PRIVATE CUSTOM_OS_EMULATOR_BUILD)
