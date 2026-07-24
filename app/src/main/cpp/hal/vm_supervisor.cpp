#include "vm_supervisor.h"

#include <android/log.h>
#include <signal.h>
#include <sys/wait.h>
#include <unistd.h>

#include <cstring>
#include <map>
#include <mutex>

#define LOG_TAG "customos_hal"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

namespace customos {

namespace {
std::mutex g_mutex;
std::map<VmHandle, pid_t> g_running;  // handle -> child pid
VmHandle g_next_handle = 0;
}  // namespace

VmSupervisor& VmSupervisor::Instance() {
    static VmSupervisor instance;
    return instance;
}

VmHandle VmSupervisor::Start(const std::vector<std::string>& argv,
                              const std::string& working_dir) {
    if (argv.empty()) {
        LOGE("Start() called with empty argv");
        return kInvalidVmHandle;
    }

    pid_t pid = fork();
    if (pid < 0) {
        LOGE("fork() failed: %s", strerror(errno));
        return kInvalidVmHandle;
    }

    if (pid == 0) {
        // Child: exec the prebuilt qemu-system-* / box64-wrapped binary.
        if (!working_dir.empty()) {
            if (chdir(working_dir.c_str()) != 0) {
                _exit(127);
            }
        }

        std::vector<char*> c_argv;
        c_argv.reserve(argv.size() + 1);
        for (const auto& s : argv) c_argv.push_back(const_cast<char*>(s.c_str()));
        c_argv.push_back(nullptr);

        execv(argv[0].c_str(), c_argv.data());
        // execv only returns on failure.
        _exit(127);
    }

    std::lock_guard<std::mutex> lock(g_mutex);
    VmHandle handle = g_next_handle++;
    g_running[handle] = pid;
    LOGI("Started VM handle=%d pid=%d binary=%s", handle, pid, argv[0].c_str());
    return handle;
}

bool VmSupervisor::Stop(VmHandle handle) {
    std::lock_guard<std::mutex> lock(g_mutex);
    auto it = g_running.find(handle);
    if (it == g_running.end()) return false;

    // Prefer letting the guest OS shut down cleanly. A more complete
    // implementation would speak the QEMU monitor protocol here
    // ("system_powerdown") instead of a bare SIGTERM.
    kill(it->second, SIGTERM);
    return true;
}

void VmSupervisor::Kill(VmHandle handle) {
    std::lock_guard<std::mutex> lock(g_mutex);
    auto it = g_running.find(handle);
    if (it == g_running.end()) return;
    kill(it->second, SIGKILL);
    int status;
    waitpid(it->second, &status, 0);
    g_running.erase(it);
}

bool VmSupervisor::IsRunning(VmHandle handle) const {
    std::lock_guard<std::mutex> lock(g_mutex);
    auto it = g_running.find(handle);
    if (it == g_running.end()) return false;
    int status;
    pid_t r = waitpid(it->second, &status, WNOHANG);
    return r == 0;  // 0 == still running
}

}  // namespace customos
