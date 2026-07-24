package com.example.customosemulator.core

import android.content.Context
import java.io.File

/**
 * Translates a [VmConfig] into a launch command for the prebuilt emulator
 * core (QEMU system emulation binary shipped in jniLibs/ - see
 * .github/workflows/build.yml for how it gets built and packaged).
 *
 * This class does NOT implement CPU emulation itself - the "Hardware
 * Emulation Layer" is the native QEMU binary. This is only the layer that
 * turns user-facing config into a concrete process invocation, analogous
 * to how Winlator/Limbo shell out to a QEMU binary with generated args.
 */
class QemuLauncher(private val context: Context) {

    private val nativeLibDir: File
        get() = File(context.applicationInfo.nativeLibraryDir)

    private fun requireBinary(name: String): File {
        val f = File(nativeLibDir, name)
        check(f.exists()) {
            "Missing native payload '$name'. Did CI package the emulator core into jniLibs?"
        }
        return f
    }

    fun buildArgs(config: VmConfig): List<String> {
        val binary = requireBinary(config.architecture.qemuSystemBinary)
        val args = mutableListOf(
            binary.absolutePath,
            "-m", config.ramMb.toString(),
            "-smp", config.vcpuCount.toString(),
            "-machine", "accel=tcg", // no KVM on unrooted Android -> TCG dynamic translation
        )

        if (config.enableGpuAcceleration) {
            // VirGL / virtio-gpu passthrough (requirement #4, "Graphics Acceleration").
            // Actual GPU backend selection (virglrenderer vs a Vulkan-based
            // ANGLE/Turnip translation layer) is a native-side concern.
            args += listOf("-device", "virtio-gpu-gl-pci")
            args += listOf("-display", "egl-headless")
        } else {
            args += listOf("-vga", "std")
        }

        args += listOf(
            "-device", "virtio-tablet-pci",   // absolute-position touch -> mouse
            "-device", "virtio-keyboard-pci",
        )

        if (config.enableNetworking) {
            args += listOf("-netdev", "user,id=net0", "-device", "virtio-net-pci,netdev=net0")
        } else {
            args += listOf("-net", "none")
        }

        config.diskImagePath?.let {
            args += listOf("-drive", "file=${it.absolutePath},if=virtio,format=raw")
        }

        config.kernelPath?.let { kernel ->
            args += listOf("-kernel", kernel.absolutePath)
            config.initramfsPath?.let { args += listOf("-initrd", it.absolutePath) }
            args += listOf("-append", config.kernelCmdline)
        }

        config.persistentDiskPath?.let {
            args += listOf("-drive", "file=${it.absolutePath},if=virtio,format=qcow2")
        }

        return args
    }
}
