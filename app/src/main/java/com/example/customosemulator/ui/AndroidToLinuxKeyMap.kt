package com.example.customosemulator.ui

import android.view.KeyEvent

/**
 * Minimal, illustrative subset of Android keycode -> Linux evdev keycode
 * mapping for guest keyboard input. Extend as needed; a full mapping table
 * (function keys, numpad, media keys, etc.) belongs here.
 */
object AndroidToLinuxKeyMap {
    private val map = mapOf(
        KeyEvent.KEYCODE_A to 30, KeyEvent.KEYCODE_B to 48, KeyEvent.KEYCODE_C to 46,
        KeyEvent.KEYCODE_ENTER to 28, KeyEvent.KEYCODE_SPACE to 57,
        KeyEvent.KEYCODE_DEL to 14, KeyEvent.KEYCODE_ESCAPE to 1,
        KeyEvent.KEYCODE_DPAD_UP to 103, KeyEvent.KEYCODE_DPAD_DOWN to 108,
        KeyEvent.KEYCODE_DPAD_LEFT to 105, KeyEvent.KEYCODE_DPAD_RIGHT to 106,
    )

    fun translate(androidKeyCode: Int): Int? = map[androidKeyCode]
}
