#include "input_injector.h"

#include <android/log.h>

#define LOG_TAG "customos_hal"

namespace customos {

// NOTE: this is a stub. Wiring this up for real requires picking a
// transport when the emulator core is integrated, e.g.:
//   - QMP (QEMU Machine Protocol) over a local unix socket for
//     `input-send-event`, or
//   - a virtio-input virtqueue written to directly if not using QEMU's
//     monitor at all.
// Both are legitimate, well-documented QEMU integration points; neither
// can be stubbed further without picking the real transport, since that
// decision depends on how the prebuilt binary from CI was configured.
void InputInjector::SendPointerEvent(VmHandle handle, float x_norm, float y_norm,
                                      int button_mask) {
    __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG,
                         "SendPointerEvent(handle=%d, x=%.3f, y=%.3f, buttons=%d) [stub]",
                         handle, x_norm, y_norm, button_mask);
    // TODO: forward over QMP `input-send-event` or virtio-input queue.
}

void InputInjector::SendKeyEvent(VmHandle handle, int linux_keycode, bool down) {
    __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG,
                         "SendKeyEvent(handle=%d, key=%d, down=%d) [stub]", handle, linux_keycode,
                         down);
    // TODO: forward over QMP `input-send-event` or virtio-input queue.
}

}  // namespace customos
