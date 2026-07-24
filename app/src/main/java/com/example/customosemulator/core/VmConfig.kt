package com.example.customosemulator.core

import java.io.File
import java.io.Serializable

/**
 * User-tunable parameters for a virtual machine instance.
 * Mirrors requirement #4 ("Tùy chỉnh tham số"): RAM, vCPU count, resolution, etc.
 *
 * This is pure configuration - it gets serialized into a QEMU argv[] (see
 * QemuLauncher.buildArgs) or an equivalent launch descriptor for whatever
 * emulation core is compiled in (QEMU system emulation, or a lighter
 * interpreter/DBT core for a constrained custom ISA).
 */
data class VmConfig(
    val name: String,
    val architecture: GuestArch = GuestArch.X86_64,
    val ramMb: Int = 2048,
    val vcpuCount: Int = 2,
    val screenWidth: Int = 1280,
    val screenHeight: Int = 720,
    val enableGpuAcceleration: Boolean = true,
    val enableNetworking: Boolean = false,

    /** Kernel image, when booting kernel+initramfs directly instead of a full disk/ISO. */
    val kernelPath: File? = null,
    val initramfsPath: File? = null,
    val kernelCmdline: String = "console=ttyS0",

    /** Full disk/ISO image (custom OS), mutually exclusive-ish with kernel boot. */
    val diskImagePath: File? = null,

    /** Persistent scratch disk for the guest OS to write to across sessions. */
    val persistentDiskPath: File? = null,
) : Serializable {
    init {
        require(ramMb in 128..8192) { "ramMb out of supported range" }
        require(vcpuCount in 1..8) { "vcpuCount out of supported range" }
        require(diskImagePath != null || kernelPath != null) {
            "VM needs either a bootable disk image or a kernel+initramfs pair"
        }
    }
}

enum class GuestArch(val qemuSystemBinary: String) {
    X86("qemu-system-i386"),
    X86_64("qemu-system-x86_64"),
    ARM("qemu-system-arm"),
    AARCH64("qemu-system-aarch64"),
    CUSTOM("qemu-system-custom"), // placeholder for a bespoke/forked target
}
