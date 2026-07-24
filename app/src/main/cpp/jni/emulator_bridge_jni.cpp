#include <android/native_window_jni.h>
#include <jni.h>

#include <string>
#include <vector>

#include "input_injector.h"
#include "surface_bridge.h"
#include "vm_supervisor.h"

using customos::InputInjector;
using customos::SurfaceBridge;
using customos::VmSupervisor;

namespace {

std::vector<std::string> JStringArrayToVector(JNIEnv* env, jobjectArray array) {
    std::vector<std::string> result;
    jsize len = env->GetArrayLength(array);
    result.reserve(len);
    for (jsize i = 0; i < len; ++i) {
        auto jstr = static_cast<jstring>(env->GetObjectArrayElement(array, i));
        const char* chars = env->GetStringUTFChars(jstr, nullptr);
        result.emplace_back(chars);
        env->ReleaseStringUTFChars(jstr, chars);
        env->DeleteLocalRef(jstr);
    }
    return result;
}

std::string JStringToStd(JNIEnv* env, jstring jstr) {
    const char* chars = env->GetStringUTFChars(jstr, nullptr);
    std::string result(chars);
    env->ReleaseStringUTFChars(jstr, chars);
    return result;
}

}  // namespace

extern "C" {

JNIEXPORT jint JNICALL
Java_com_example_customosemulator_core_EmulatorBridge_nativeStartVm(
        JNIEnv* env, jobject /* thiz */, jobjectArray argv, jstring working_dir) {
    std::vector<std::string> args = JStringArrayToVector(env, argv);
    std::string cwd = JStringToStd(env, working_dir);
    return static_cast<jint>(VmSupervisor::Instance().Start(args, cwd));
}

JNIEXPORT jboolean JNICALL
Java_com_example_customosemulator_core_EmulatorBridge_nativeStopVm(
        JNIEnv* /* env */, jobject /* thiz */, jint handle) {
    return VmSupervisor::Instance().Stop(handle) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_example_customosemulator_core_EmulatorBridge_nativeKillVm(
        JNIEnv* /* env */, jobject /* thiz */, jint handle) {
    VmSupervisor::Instance().Kill(handle);
}

JNIEXPORT void JNICALL
Java_com_example_customosemulator_core_EmulatorBridge_nativeAttachSurface(
        JNIEnv* env, jobject /* thiz */, jint handle, jobject surface) {
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    SurfaceBridge::Attach(handle, window);
}

JNIEXPORT void JNICALL
Java_com_example_customosemulator_core_EmulatorBridge_nativeDetachSurface(
        JNIEnv* /* env */, jobject /* thiz */, jint handle) {
    SurfaceBridge::Detach(handle);
}

JNIEXPORT void JNICALL
Java_com_example_customosemulator_core_EmulatorBridge_nativeSendPointerEvent(
        JNIEnv* /* env */, jobject /* thiz */, jint handle, jfloat x_norm, jfloat y_norm,
        jint button_mask) {
    InputInjector::SendPointerEvent(handle, x_norm, y_norm, button_mask);
}

JNIEXPORT void JNICALL
Java_com_example_customosemulator_core_EmulatorBridge_nativeSendKeyEvent(
        JNIEnv* /* env */, jobject /* thiz */, jint handle, jint linux_keycode, jboolean down) {
    InputInjector::SendKeyEvent(handle, linux_keycode, down == JNI_TRUE);
}

JNIEXPORT void JNICALL
Java_com_example_customosemulator_core_EmulatorBridge_nativeUpdateResourceLimits(
        JNIEnv* /* env */, jobject /* thiz */, jint handle, jint vcpu_count, jint ram_mb) {
    // TODO: forward to QMP `device_add`/`balloon` or equivalent live-resize
    // hooks once the emulator core transport is wired up.
    (void) handle;
    (void) vcpu_count;
    (void) ram_mb;
}

}  // extern "C"
