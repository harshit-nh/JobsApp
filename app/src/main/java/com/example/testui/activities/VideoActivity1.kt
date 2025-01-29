package com.example.testui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testui.R
import com.example.testui.databinding.ActivityVideo1Binding

class VideoActivity1 : AppCompatActivity() {

    private lateinit var binding: ActivityVideo1Binding

    private val videoCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val videoUri: Uri? = result.data?.data
                videoUri?.let {
                    navigateToVideoActivity2(it)
                }
            } else {
                Toast.makeText(this, "Video recording cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideo1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.videoActivityMain1)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = binding.toolbar3
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.black))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val videoBtn = binding.makeVideoBtn

        val goBackBtn = binding.goBackBtn

        goBackBtn.setOnClickListener {
            onBackPressed()
        }

        videoBtn.setOnClickListener {
            requestPermission()
        }
    }

    // Request camera and audio permissions if not already granted
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            val cameraGranted = permissions[Manifest.permission.CAMERA] ?: false

            if (cameraGranted ) {
                openCameraForVideoRecording()
            } else {
                Toast.makeText(
                    this,
                    "Camera and Audio permissions are required to record video",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    // Checking if permissions are already granted or request them
    private fun requestPermission() {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (cameraPermission == PackageManager.PERMISSION_GRANTED ) {
            openCameraForVideoRecording()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA
                )
            )
        }
    }

    // Opening camera to start video recording
    private fun openCameraForVideoRecording() {
        val packageManager = packageManager
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)

        // Checking if a camera app is available
        if (intent.resolveActivity(packageManager) != null) {
            videoCaptureLauncher.launch(intent)
        } else {
            Toast.makeText(this, "No suitable app found to record video", Toast.LENGTH_SHORT).show()
        }
    }

    // Navigating to the next activity with the captured video URI
    private fun navigateToVideoActivity2(videoUri: Uri?) {
        if (videoUri != null) {
            val intent = Intent(this, VideoActivity2::class.java).apply {
                putExtra("videoUri", videoUri.toString())
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Failed to capture video", Toast.LENGTH_SHORT).show()
        }
    }
}
