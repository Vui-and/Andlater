#pragma once

#include <string>
#include <vector>

namespace customos {

// Opaque handle referring to one running guest VM process.
using VmHandle = int;
constexpr VmHandle kInvalidVmHandle = -1;

// Owns fork/exec + lifecycle of one prebuilt qemu-system-* (or box64-wrapped
// custom interpreter) subprocess. This is the "Hardware Emulation Layer"
// process boundary: Android does not allow linking arbitrary GPL system
// emulators directly for this design, so the prebuilt binary is run as a
// separate process, matching the approach used by Winlator/Limbo.
class VmSupervisor {
public:
    static VmSupervisor& Instance();

    // argv[0] must be an absolute path to a prebuilt binary inside
    // applicationInfo.nativeLibraryDir (see QemuLauncher.kt).
    VmHandle Start(const std::vector<std::string>& argv, const std::string& working_dir);

    // Requests graceful shutdown (SIGTERM / QEMU monitor "system_powerdown").
    bool Stop(VmHandle handle);

    // SIGKILL fallback.
    void Kill(VmHandle handle);

    bool IsRunning(VmHandle handle) const;

private:
    VmSupervisor() = default;
};

}  // namespace customos
