#pragma once

#include "vm_supervisor.h"

namespace customos {

// Forwards touch/keyboard events into a running guest VM. Real
// implementation depends on the transport chosen for the emulator core:
//   - If QEMU is run with `-qmp` / a virtio-input socket, this translates
//     calls into that protocol.
//   - If a bespoke/custom-ISA interpreter is used instead of QEMU, this
//     would write into whatever virtual input device that core exposes.
class InputInjector {
public:
    static void SendPointerEvent(VmHandle handle, float x_norm, float y_norm, int button_mask);
    static void SendKeyEvent(VmHandle handle, int linux_keycode, bool down);
};

}  // namespace customos
