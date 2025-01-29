package com.example.testui.activities

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.testui.LoadingAnim
import com.example.testui.MainActivity
import com.example.testui.R
import com.example.testui.databinding.ActivityVideo2Binding
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VideoActivity2 : LoadingAnim() {


    private lateinit var binding: ActivityVideo2Binding

    private lateinit var videoView:VideoView

    private lateinit var uploadBtn:MaterialButton

    private lateinit var jobId:String

    private lateinit var videoUri: Uri

    private lateinit var firestore: FirebaseFirestore

    private lateinit var cloudinary:Cloudinary

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideo2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.videoActivityMain2)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        val toolbar = binding.toolbar4
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.black))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        videoView = binding.videoView

        val recordAgainBtn = binding.recordAgainBtn

        uploadBtn = binding.uploadVideoBtn

        val sharedPreferences = getSharedPreferences("job_data", Context.MODE_PRIVATE)
        jobId = sharedPreferences.getString("jobId","Null").toString()

        recordAgainBtn.setOnClickListener {
            startActivity(Intent(this, VideoActivity1::class.java))
            finish()
        }

        cloudinary = Cloudinary(
            ObjectUtils.asMap(
            "cloud_name", "deistlvd2",
            "api_key", "459832882653897",
            "api_secret", "kgTVIR7-QeEbAYTiJW2j41ld4Kw"
        ))

        val videoUriString = intent.getStringExtra("videoUri")
        videoUriString?.let{

            videoUri = Uri.parse(it)
            videoView.setVideoURI(videoUri)

            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            videoView.start()
        }

        uploadBtn.setOnClickListener {
            uploadVideoToCloudinary(videoUri)
        }

    }


    private fun getRealPathFromURI(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, projection, null, null, null)
        val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
        cursor?.moveToFirst()

        return cursor?.getString(columnIndex ?: 0)
    }


    private fun uploadVideoToCloudinary(videoUri: Uri){

        showLoadingDialog()

        val videoFile = getRealPathFromURI(videoUri)?.let {
            File(it)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try{
                val uploadResponse = cloudinary.uploader().upload(
                    videoFile,
                    ObjectUtils.asMap("resource_type","video")
                )

                withContext(Dispatchers.Main){
                    hideLoadingDialog()

                    Log.d("CloudUpload", "Video uploaded successfully: $uploadResponse")
                    Toast.makeText(this@VideoActivity2, "Video uploaded successfully", Toast.LENGTH_SHORT).show()

                    changeProgressState()

                    val intent = Intent(this@VideoActivity2, CloseJobActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

            }catch (e: Exception){
                withContext(Dispatchers.Main){
                    hideLoadingDialog()
                    Log.e("CloudUpload", "Upload failed: ${e.localizedMessage}")
                    Toast.makeText(this@VideoActivity2, "Video upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun changeProgressState() {

        firestore.collection("jobs_data").whereEqualTo("jobId",jobId).get()

            .addOnSuccessListener { querySnapshot ->
                if(!querySnapshot.isEmpty){
                    val document = querySnapshot.documents[0]
                    val documentId = document.id

                    firestore.collection("jobs_data")
                        .document(documentId)
                        .update("progressState","UploadVideo")
                        .addOnSuccessListener {
                            Log.d("Firestore", "Progress state updated successfully")
                        }
                        .addOnFailureListener { exception ->
                            Log.d("Firestore", "Error updating progress state: ${exception.localizedMessage}")
                        }
                }else{
                    Log.d("Firestore", "No document found with JobId: $jobId")
                }
            }
            .addOnFailureListener {
                Log.d("Firestore", "Error querying firestore")
            }

    }
}