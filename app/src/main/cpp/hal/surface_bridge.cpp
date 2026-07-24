#include "surface_bridge.h"

#include <android/log.h>

#define LOG_TAG "customos_hal"

namespace customos {

// NOTE: stub. Real implementation needs to pick strategy (1) or (2) from
// the header comment, which depends on how the emulator core built by CI
// exposes its framebuffer (egl-headless DMA-BUF, shared memory, etc).
void SurfaceBridge::Attach(VmHandle handle, ANativeWindow* window) {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "SurfaceBridge::Attach(handle=%d) [stub]",
                         handle);
    // TODO: start a render/copy loop from the emulator core's framebuffer
    // into `window` (ANativeWindow_lock / ANativeWindow_unlockAndPost for
    // the simple path).
}

void SurfaceBridge::Detach(VmHandle handle) {
    __android_log_print(ANDROID_LOG_INFO, LOG_TAG, "SurfaceBridge::Detach(handle=%d) [stub]",
                         handle);
    // TODO: stop the render/copy loop started in Attach().
}

}  // namespace customos
