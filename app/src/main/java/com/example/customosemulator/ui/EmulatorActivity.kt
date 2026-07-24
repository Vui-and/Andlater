package com.example.customosemulator.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.customosemulator.core.EmulatorBridge
import com.example.customosemulator.core.QemuLauncher
import com.example.customosemulator.core.VmConfig
import com.example.customosemulator.databinding.ActivityEmulatorBinding
import com.example.customosemulator.hal.TouchToPointerTranslator

/**
 * "UI Layer" per requirement #3: shows the VM's framebuffer, forwards
 * touch as mouse events, and toggles a soft keyboard for guest text input.
 *
 * VM process lifecycle itself lives in EmulatorService; this Activity only
 * owns the Surface and forwards input, so rotating the screen or briefly
 * backgrounding the app doesn't kill the guest OS.
 */
class EmulatorActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityEmulatorBinding
    private lateinit var config: VmConfig
    private val launcher = QemuLauncher(this)
    private val pointerTranslator = TouchToPointerTranslator()
    private var vmHandle: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        @Suppress("DEPRECATION")
        config = intent.getSerializableExtra(EXTRA_VM_CONFIG) as? VmConfig
            ?: error("EmulatorActivity requires EXTRA_VM_CONFIG")

        binding = ActivityEmulatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.surfaceVmScreen.holder.addCallback(this)

        binding.buttonToggleKeyboard.setOnClickListener {
            binding.editVirtualKeyboardProxy.requestFocus()
            // System soft-keyboard pops up on focus; key events are read in
            // editVirtualKeyboardProxy's key listener and forwarded below.
        }

        binding.surfaceVmScreen.setOnTouchListener { view, event ->
            handleTouch(view.width, view.height, event)
            true
        }

        binding.editVirtualKeyboardProxy.setOnKeyListener { _, keyCode, event ->
            if (vmHandle >= 0) {
                val linuxKeyCode = AndroidToLinuxKeyMap.translate(keyCode)
                if (linuxKeyCode != null) {
                    EmulatorBridge.nativeSendKeyEvent(
                        vmHandle, linuxKeyCode, event.action == android.view.KeyEvent.ACTION_DOWN
                    )
                    return@setOnKeyListener true
                }
            }
            false
        }
    }

    private fun handleTouch(viewWidth: Int, viewHeight: Int, event: MotionEvent) {
        if (vmHandle < 0) return
        val (xNorm, yNorm, buttonMask) = pointerTranslator.translate(viewWidth, viewHeight, event)
        EmulatorBridge.nativeSendPointerEvent(vmHandle, xNorm, yNorm, buttonMask)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        vmHandle = launcher.let { l ->
            EmulatorBridge.nativeStartVm(
                l.buildArgs(config).toTypedArray(),
                filesDir.absolutePath,
            )
        }
        EmulatorBridge.nativeAttachSurface(vmHandle, holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) = Unit

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        if (vmHandle >= 0) {
            EmulatorBridge.nativeDetachSurface(vmHandle)
        }
    }

    override fun onDestroy() {
        if (vmHandle >= 0) {
            EmulatorBridge.nativeStopVm(vmHandle)
        }
        super.onDestroy()
    }

    companion object {
        const val EXTRA_VM_CONFIG = "extra_vm_config"
    }
}
