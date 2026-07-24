#pragma once

#include <android/native_window.h>

#include "vm_supervisor.h"

namespace customos {

// Connects the emulator core's rendered output to an Android Surface.
//
// Two realistic strategies, in increasing order of performance/complexity:
//   1. Pixel-copy fallback: emulator core renders to an offscreen buffer
//      (e.g. QEMU's egl-headless display), and this class blits it into
//      the ANativeWindow each frame. Simple, CPU-bound, fine for a first
//      bring-up.
//   2. Zero-copy: emulator core renders via virglrenderer/Vulkan directly
//      into a buffer shared with the ANativeWindow (AHardwareBuffer +
//      EGLImage import), avoiding the CPU copy. This is what a
//      production-quality implementation (a la Winlator) needs for
//      acceptable 3D performance, corresponding to requirement #4's
//      "Graphics Acceleration" ask.
class SurfaceBridge {
public:
    static void Attach(VmHandle handle, ANativeWindow* window);
    static void Detach(VmHandle handle);
};

}  // namespace customos
