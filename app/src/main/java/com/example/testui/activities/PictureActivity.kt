package com.example.testui.activities


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.testui.R
import com.example.testui.databinding.ActivityPictureBinding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class PictureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPictureBinding
    private lateinit var imageView: ImageView
    private lateinit var imageUri: Uri
    private lateinit var imageFile: File


    private val permissionsRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions[Manifest.permission.CAMERA] == true) {
                // Permissions granted, proceed with taking a picture
                imageUri = createImageUri() // Create URI for captured image
                contract.launch(imageUri)
            } else {
                // Permissions denied, show message
                Toast.makeText(this, "Camera or Storage permissions are required", Toast.LENGTH_SHORT).show()
            }
        }

    private val contract =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { result ->

            if (result) {
                compressAndSaveImage()
            } else {
                Log.e("PictureActivity", "Failed to capture image")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPictureBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.pictureActivityMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar = binding.toolbar2
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.black))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val skipBtn = binding.skipBtn1
        val clickPictureBtn = binding.clickPictureBtn

        imageView = binding.bankImage

        skipBtn.setOnClickListener {

        }

        clickPictureBtn.setOnClickListener {

            if(checkPermissions()){
                imageUri = createImageUri()
                contract.launch(imageUri)
            }else{
                permissionsRequest.launch(
                    arrayOf(
                        Manifest.permission.CAMERA
                    )
                )
            }

        }

    }


    private fun createImageUri(): Uri {

        imageFile = File(applicationContext.filesDir, "location_photo.jpg")
        return FileProvider.getUriForFile(
            applicationContext,
            "com.example.testui.fileProvider",
            imageFile
        )
    }


    // Check if camera and storage permissions are granted
    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun compressAndSaveImage() {

        lifecycleScope.launch(Dispatchers.IO) {

            try {
                // Use Glide to load and resize the image
                val bitmap = Glide.with(this@PictureActivity)
                    .asBitmap()
                    .load(imageFile)
                    .apply(
                        RequestOptions()
                        .format(DecodeFormat.PREFER_RGB_565)  // Reduce color quality to reduce size
                        .override(1280, 720)  // Resize image to 1280x720
                        .encodeQuality(80)
                    )
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)// Set quality to 80%
                    .submit()
                    .get()  // Get the compressed image bitmap

                val compressedImageFile = File(applicationContext.filesDir, "location_photo.jpg")

                // Save the compressed bitmap to file
                FileOutputStream(compressedImageFile).use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream) // Save as JPEG with 80% quality
                    outStream.flush()
                }

                // Navigate to next activity with the compressed image URI
                launch(Dispatchers.Main) {
                    val compressedImageUri = Uri.fromFile(compressedImageFile)
                    val intent = Intent(this@PictureActivity, UploadPicActivity::class.java)
                    intent.putExtra("capturedImageUri", compressedImageUri.toString())
                    startActivity(intent)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Log.e("PictureActivity", "Failed to compress: ${e.localizedMessage}")
                    Toast.makeText(this@PictureActivity, "Image compression failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

}