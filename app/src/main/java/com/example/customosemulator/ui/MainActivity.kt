package com.example.customosemulator.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.customosemulator.core.GuestArch
import com.example.customosemulator.core.VmConfig
import com.example.customosemulator.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream

/**
 * Setup screen implementing requirement #2 (user supplies a custom OS
 * image) and requirement #4 (tunable RAM / vCPU / resolution).
 *
 * The picked image is copied into app-private storage so QemuLauncher can
 * hand QEMU a plain filesystem path (SAF content:// URIs aren't directly
 * usable by a native subprocess).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var importedDiskImage: File? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { importOsImage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonPickImage.setOnClickListener {
            // .iso / .img / raw kernel images - MIME type is unreliable for
            // these so we accept */* and validate by content afterwards.
            pickImage.launch("*/*")
        }

        binding.buttonStart.setOnClickListener { startVm() }
    }

    private fun importOsImage(uri: Uri) {
        val displayName = queryDisplayName(uri) ?: "custom_os_image.img"
        val dest = File(getExternalFilesDir("images"), displayName)
        contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        importedDiskImage = dest
        binding.textSelectedImage.text = displayName
    }

    private fun queryDisplayName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) return cursor.getString(idx)
        }
        return null
    }

    private fun startVm() {
        val disk = importedDiskImage ?: run {
            binding.textSelectedImage.error = "Chọn file .iso/.img trước"
            return
        }

        val config = VmConfig(
            name = binding.editVmName.text?.toString()?.ifBlank { "custom-os" } ?: "custom-os",
            architecture = selectedArch(),
            ramMb = binding.seekRam.progress.coerceAtLeast(256),
            vcpuCount = binding.seekVcpu.progress.coerceAtLeast(1),
            screenWidth = 1280,
            screenHeight = 720,
            enableGpuAcceleration = binding.switchGpu.isChecked,
            enableNetworking = binding.switchNetwork.isChecked,
            diskImagePath = disk,
        )

        val intent = Intent(this, EmulatorActivity::class.java).apply {
            putExtra(EmulatorActivity.EXTRA_VM_CONFIG, config)
        }
        startActivity(intent)
    }

    private fun selectedArch(): GuestArch =
        when (binding.spinnerArch.selectedItem?.toString()) {
            "x86" -> GuestArch.X86
            "arm" -> GuestArch.ARM
            "aarch64" -> GuestArch.AARCH64
            "custom" -> GuestArch.CUSTOM
            else -> GuestArch.X86_64
        }
}
