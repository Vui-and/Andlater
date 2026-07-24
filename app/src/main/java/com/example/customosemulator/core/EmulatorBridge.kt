package com.example.customosemulator.core

import android.view.Surface

/**
 * JNI boundary between Kotlin and the native "Hardware Emulation Layer" +
 * "OS Compatibility Layer" (C/C++, built via app/src/main/cpp/CMakeLists.txt).
 *
 * The native side is expected to either:
 *   (a) fork/exec the prebuilt qemu-system-* binary using the argv from
 *       QemuLauncher and manage its lifecycle (simplest, matches how
 *       Winlator/Limbo operate), or
 *   (b) link QEMU as a library and drive it directly for tighter control
 *       over the framebuffer callback / audio / input queues.
 *
 * This skeleton assumes (a) for the process boundary and only uses JNI for
 * lifecycle control + frame delivery, since QEMU-as-subprocess is far easier
 * to get building reliably on Android than qemu-as-a-library.
 */
object EmulatorBridge {

    init {
        System.loadLibrary("customos_hal") // see cpp/CMakeLists.txt target `customos_hal`
    }

    /** Starts the native supervisor process with the given argv. Returns a PID/handle. */
    external fun nativeStartVm(argv: Array<String>, workingDir: String): Int

    /** Requests graceful shutdown (ACPI-style) of a running VM handle. */
    external fun nativeStopVm(handle: Int): Boolean

    /** Hard-kill fallback if the guest OS is unresponsive. */
    external fun nativeKillVm(handle: Int)

    /** Attaches the framebuffer output to an Android Surface (e.g. from a SurfaceView/TextureView). */
    external fun nativeAttachSurface(handle: Int, surface: Surface)

    external fun nativeDetachSurface(handle: Int)

    /** Injects a synthetic mouse event (absolute coords, 0..1 normalized) into the guest. */
    external fun nativeSendPointerEvent(handle: Int, xNorm: Float, yNorm: Float, buttonMask: Int)

    /** Injects a synthetic key event (evdev/Linux keycode) into the guest. */
    external fun nativeSendKeyEvent(handle: Int, linuxKeyCode: Int, down: Boolean)

    /** Live-adjusts vCPU throttling / RAM ballooning where the guest supports it. */
    external fun nativeUpdateResourceLimits(handle: Int, vcpuCount: Int, ramMb: Int)
}
