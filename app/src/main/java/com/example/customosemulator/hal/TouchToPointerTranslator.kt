package com.example.customosemulator.hal

import android.view.MotionEvent

/**
 * Part of the "Hardware Emulation Layer" bridge on the Kotlin side: maps
 * Android touch semantics onto the absolute-pointer (virtio-tablet) device
 * model QemuLauncher configures, since a touchscreen doesn't have relative
 * mouse-style deltas.
 */
class TouchToPointerTranslator {

    data class Result(val xNorm: Float, val yNorm: Float, val buttonMask: Int)

    fun translate(viewWidth: Int, viewHeight: Int, event: MotionEvent): Result {
        val xNorm = (event.x / viewWidth.coerceAtLeast(1)).coerceIn(0f, 1f)
        val yNorm = (event.y / viewHeight.coerceAtLeast(1)).coerceIn(0f, 1f)

        val buttonMask = when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> BUTTON_LEFT
            else -> 0
        }

        return Result(xNorm, yNorm, buttonMask)
    }

    companion object {
        const val BUTTON_LEFT = 1 shl 0
        const val BUTTON_RIGHT = 1 shl 1
        const val BUTTON_MIDDLE = 1 shl 2
    }
}
