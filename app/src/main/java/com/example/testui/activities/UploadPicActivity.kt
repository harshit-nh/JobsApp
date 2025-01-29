package com.example.testui.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.testui.LoadingAnim
import com.example.testui.R
import com.example.testui.databinding.ActivityUploadPicBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class UploadPicActivity : LoadingAnim() {

    private lateinit var binding:ActivityUploadPicBinding
    private lateinit var cloudinary:Cloudinary
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUploadPicBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.uploadPictureMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()


        //checking if the image clicked previously exists in storage or not
        checkSavedImage()

        val toolbar = binding.toolbar3
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.black))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        cloudinary = Cloudinary(ObjectUtils.asMap(
            "cloud_name", "deistlvd2",
            "api_key", "459832882653897",
            "api_secret", "kgTVIR7-QeEbAYTiJW2j41ld4Kw"
        ))

        val skipBtn = binding.reClickBtn
        val uploadPicBtn = binding.uploadPictureBtn

        val sharedPreferences = getSharedPreferences("job_data", Context.MODE_PRIVATE)
        val jobId = sharedPreferences.getString("jobId","Null")


        val imageUriString = intent.getStringExtra("capturedImageUri")

        if(imageUriString != null){

//            val imageUri = Uri.parse(imageUriString)

            Glide.with(this)
                .load(imageUriString)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.pictureView)

            if (jobId != null) {

                firestore.collection("jobs_data").whereEqualTo("jobId",jobId).get()

                    .addOnSuccessListener { querySnapshot ->
                        if(!querySnapshot.isEmpty){
                            val document = querySnapshot.documents[0]
                            val documentId = document.id

                            firestore.collection("jobs_data")
                                .document(documentId)
                                .update("progressState","UploadPic")
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

        }else{
            Log.d("TAG", "Failed to load image")
        }

        uploadPicBtn.setOnClickListener {
            val imageUri = Uri.parse(imageUriString)?: Uri.parse(File(applicationContext.filesDir,"location_photo.jpg").toString())
            uploadImageToCloudinary(imageUri)
        }


        skipBtn.setOnClickListener {
            startActivity(Intent(this, PictureActivity::class.java))
            finish()
        }
    }


    private fun uploadImageToCloudinary(imageUri: Uri){

        val filePath = imageUri.path
        val file = filePath?.let { File(it) }

        showLoadingDialog()

        lifecycleScope.launch(Dispatchers.IO) {


            try{
                val response = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())


                Log.d("CloudUpload", "Upload Successful: $response")
                withContext(Dispatchers.Main){

                    hideLoadingDialog()
                    Toast.makeText(this@UploadPicActivity, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@UploadPicActivity,UploadDocsActivity::class.java))
                }
            }catch(e:Exception){

                withContext(Dispatchers.Main) {
                    hideLoadingDialog()
                    Log.e("CloudUpload", "Upload failed: ${e.localizedMessage}")
                    Toast.makeText(this@UploadPicActivity, "Upload failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun checkSavedImage(){

        val imageFile = File(applicationContext.filesDir,"location_photo.jpg")
        if(imageFile.exists()){
            binding.pictureView.setImageURI(Uri.fromFile(imageFile))
            binding.uploadPictureBtn.setOnClickListener {
                startActivity(Intent(this,UploadDocsActivity::class.java))
            }
        }else{
            Log.d("Check Pic", "location_photo does not exists")
        }
    }


}