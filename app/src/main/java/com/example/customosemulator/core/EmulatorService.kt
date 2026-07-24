package com.example.customosemulator.core

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.customosemulator.R

/**
 * Keeps the emulated machine alive across rotations / the user briefly
 * switching apps, similar to how Winlator runs its session as a foreground
 * service. Actual VM state lives in native code (see EmulatorBridge); this
 * class is just the Android lifecycle wrapper + notification.
 */
class EmulatorService : Service() {

    private var vmHandle: Int = -1
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): EmulatorService = this@EmulatorService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    fun launch(config: VmConfig, launcher: QemuLauncher): Int {
        val args = launcher.buildArgs(config)
        vmHandle = EmulatorBridge.nativeStartVm(
            args.toTypedArray(),
            filesDir.absolutePath,
        )
        startForeground(NOTIFICATION_ID, buildNotification(config.name))
        return vmHandle
    }

    fun shutdown() {
        if (vmHandle >= 0) {
            EmulatorBridge.nativeStopVm(vmHandle)
            vmHandle = -1
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        if (vmHandle >= 0) EmulatorBridge.nativeKillVm(vmHandle)
        super.onDestroy()
    }

    private fun buildNotification(vmName: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Custom OS Emulator")
            .setContentText("Đang chạy: $vmName")
            .setSmallIcon(R.drawable.ic_vm_running)
            .setOngoing(true)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Emulator session", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "emulator_session"
        private const val NOTIFICATION_ID = 1001
    }
}
