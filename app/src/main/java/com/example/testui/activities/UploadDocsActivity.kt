package com.example.testui.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.testui.LoadingAnim
import com.example.testui.R
import com.example.testui.databinding.ActivityUploadDocsBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class UploadDocsActivity : LoadingAnim() {

    private lateinit var binding:ActivityUploadDocsBinding

    private lateinit var aadharFrontUri: Uri
    private lateinit var aadharBackUri: Uri
    private lateinit var panFrontUri: Uri
    private lateinit var othersFrontUri: Uri

    private lateinit var aadharFrontReUploadImg:ImageView
    private lateinit var aadharBackReUploadImg:ImageView
    private lateinit var panFrontReUploadImg:ImageView
    private lateinit var othersFrontReUploadImg:ImageView

    private lateinit var imageFile:File

    private var currentImage:Int = 0

    private lateinit var cloudinary: Cloudinary

    private lateinit var goBackBtn: MaterialButton

    private lateinit var firestore: FirebaseFirestore
    private lateinit var jobId:String

    private val isUploaded = MutableList(4) {false}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityUploadDocsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.uploadDocsmain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        val toolbar = binding.toolbar5
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.black))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        val aadharFrontImg = binding.aadharFrontImg
        val aadharBackImg = binding.aadharBackImg
        val panFrontImg = binding.panFrontImg
        val othersFrontImg = binding.otherFrontImg
        val uploadDocsBtn = binding.uploadDocBtn
        val completeBtn = binding.completeBtn

        aadharFrontReUploadImg = binding.uploadAadharFrontImg
        aadharBackReUploadImg = binding.uploadAadharBackImg
        panFrontReUploadImg = binding.uploadPanFrontImg
        othersFrontReUploadImg = binding.uploadOtherFrontImg


        val sharedPreferences = getSharedPreferences("job_data", Context.MODE_PRIVATE)
        jobId = sharedPreferences.getString("jobId","Null").toString()

        val verifyTick1 = binding.verifyTick1
        val verifyTick2 = binding.verifyTick2
        val verifyTick3 = binding.verifyTick3
        val verifyTick4 = binding.verifyTick4

        cloudinary = Cloudinary(ObjectUtils.asMap(
            "cloud_name", "deistlvd2",
            "api_key", "459832882653897",
            "api_secret", "kgTVIR7-QeEbAYTiJW2j41ld4Kw"
        ))

        aadharFrontUri = createImageUri("aadhar_front.jpg")
        aadharBackUri = createImageUri("aadhar_back.jpg")
        panFrontUri = createImageUri("pan_front.jpg")
        othersFrontUri = createImageUri("others_front.jpg")

//        checkSavedImages()

        goBackBtn = binding.goBackBtn1
        goBackBtn.setOnClickListener {
            startActivity(Intent(this, UploadPicActivity::class.java))
            finish()
        }

        aadharFrontImg.setOnClickListener {
            currentImage = 0
            contract.launch(aadharFrontUri)
        }

        aadharBackImg.setOnClickListener {
            if(!isUploaded[0]){
                Toast.makeText(this@UploadDocsActivity, "Please upload Aadhar front first", Toast.LENGTH_SHORT).show()
            }else{
                currentImage = 1
                contract.launch(aadharBackUri)
            }
        }

        panFrontImg.setOnClickListener {
            if(!isUploaded[1]){
                Toast.makeText(this@UploadDocsActivity, "Please upload Aadhar back first", Toast.LENGTH_SHORT).show()
            }else {
                currentImage = 2
                contract.launch(panFrontUri)
            }
        }

        othersFrontImg.setOnClickListener {
            if(!isUploaded[2]){
                Toast.makeText(this@UploadDocsActivity, "Please upload PAN front first", Toast.LENGTH_SHORT).show()
            }else {
                currentImage = 3
                contract.launch(othersFrontUri)
            }
        }

        aadharFrontReUploadImg.setOnClickListener {
            currentImage = 0
            contract.launch(aadharFrontUri)
            verifyTick1.visibility = View.GONE
        }

        aadharBackReUploadImg.setOnClickListener {
            currentImage = 1
            contract.launch(aadharBackUri)
            verifyTick2.visibility = View.GONE
        }

        panFrontReUploadImg.setOnClickListener {
            currentImage = 2
            contract.launch(panFrontUri)
            verifyTick3.visibility = View.GONE
        }

        othersFrontReUploadImg.setOnClickListener {
            currentImage = 3
            contract.launch(othersFrontUri)
            verifyTick4.visibility = View.GONE
            uploadDocsBtn.visibility = View.VISIBLE
            completeBtn.visibility = View.GONE
        }



        uploadDocsBtn.setOnClickListener {

            when(currentImage){
                0 -> {
                    uploadDoc("Aadhar Front", File(applicationContext.filesDir, "aadhar_front.jpg"))
                    isUploaded[0] = true
                    verifyTick1.visibility = View.VISIBLE
                    aadharFrontImg.isEnabled = false
                    aadharFrontReUploadImg.visibility = View.VISIBLE
                }
                1 -> {
                    uploadDoc("Aadhar Back", File(applicationContext.filesDir, "aadhar_back.jpg"))
                    isUploaded[1] = true
                    verifyTick2.visibility = View.VISIBLE
                    aadharBackImg.isEnabled = false
                    aadharBackReUploadImg.visibility = View.VISIBLE
                }
                2 -> {
                    uploadDoc("PAN front", File(applicationContext.filesDir, "pan_front.jpg"))
                    isUploaded[2] = true
                    verifyTick3.visibility = View.VISIBLE
                    panFrontImg.isEnabled = false
                    panFrontReUploadImg.visibility = View.VISIBLE
                }
                3 -> {
                    uploadDoc("Other Document", File(applicationContext.filesDir, "others_front.jpg"))
                    isUploaded[3] = true
                    verifyTick4.visibility = View.VISIBLE
                    othersFrontImg.isEnabled = false
                    othersFrontReUploadImg.visibility = View.VISIBLE
                    uploadDocsBtn.visibility = View.GONE
                    completeBtn.visibility = View.VISIBLE
                }
            }


            uploadDocsBtn.isEnabled = false
        }

        completeBtn.setOnClickListener {
            startActivity(Intent(this,VideoActivity1::class.java))
        }


    }


    private fun createImageUri(fileName: String): Uri {
        imageFile = File(applicationContext.filesDir,fileName)
        return FileProvider.getUriForFile(
            applicationContext,
            "com.example.testui.fileProvider",
            imageFile
        )
    }


    private val contract = registerForActivityResult(ActivityResultContracts.TakePicture()){ success ->
        if(success){
            when (currentImage) {
                0 -> {
                    compressAndSaveImage("aadhar_front.jpg", aadharFrontUri, binding.aadharFrontImg)
                }
                1 ->{
                    compressAndSaveImage("aadhar_back.jpg", aadharBackUri, binding.aadharBackImg)
                }
                2 -> {
                    compressAndSaveImage("pan_front.jpg", panFrontUri, binding.panFrontImg)
                }
                3 -> {
                    compressAndSaveImage("others_front.jpg", othersFrontUri, binding.otherFrontImg)
                }
            }

            binding.uploadDocBtn.isEnabled = true
        }
    }

    private fun compressAndSaveImage(imageFileName:String, imageUri:Uri, imageView: ImageView) {

        lifecycleScope.launch(Dispatchers.IO) {

            try {
                // Use Glide to load and resize the image
                val bitmap = Glide.with(this@UploadDocsActivity)
                    .asBitmap()
                    .load(imageUri)
                    .apply(
                        RequestOptions()
                            .format(DecodeFormat.PREFER_RGB_565)  // Reduce color quality to reduce size
                            .override(1280, 720)  // Resize image to 1280x720
                            .encodeQuality(80)
                    )
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)// Set quality to 80%
                    .submit()
                    .get() // Get the compressed image bitmap

                val compressedImageFile = File(applicationContext.filesDir, imageFileName)

                // Save the compressed bitmap to file
                FileOutputStream(compressedImageFile).use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream) // Save as JPEG with 80% quality
                    outStream.flush()
                }


                launch(Dispatchers.Main) {

                    val compressedImageUri = Uri.fromFile(compressedImageFile)
                    imageView.setImageURI(Uri.fromFile(compressedImageFile))

                    Glide.with(this@UploadDocsActivity)
                        .load(compressedImageUri)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(imageView)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Log.e("UploadDocsActivity", "Failed to compress: ${e.localizedMessage}")
                    Toast.makeText(this@UploadDocsActivity, "Image compression failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun uploadDoc(docName:String, compressedImageFile:File){

        showLoadingDialog()

        lifecycleScope.launch(Dispatchers.IO) {

            try{
                val response = cloudinary.uploader().upload(compressedImageFile, ObjectUtils.emptyMap())

                Log.d("CloudUpload", "Upload Successful: $response")
                withContext(Dispatchers.Main){

                    hideLoadingDialog()
                    Toast.makeText(this@UploadDocsActivity, "$docName uploaded successfully", Toast.LENGTH_SHORT).show()

                    firestore.collection("jobs_data").whereEqualTo("jobId",jobId).get()

                        .addOnSuccessListener { querySnapshot ->
                            if(!querySnapshot.isEmpty){
                                val document = querySnapshot.documents[0]
                                val documentId = document.id

                                firestore.collection("jobs_data")
                                    .document(documentId)
                                    .update("progressState","UploadDocs")
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
            }catch(e:Exception){

                withContext(Dispatchers.Main) {
                    hideLoadingDialog()
                    Log.e("CloudUpload", "Upload failed: ${e.localizedMessage}")
                    Toast.makeText(this@UploadDocsActivity, "Upload failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }


//    private fun checkSavedImages(){
//
//        val imageMappings = listOf(
//            Pair(binding.aadharFrontImg,"aadhar_front.jpg"),
//            Pair(binding.aadharBackImg, "aadhar_back.jpg"),
//            Pair(binding.panFrontImg, "pan_front.jpg"),
//            Pair(binding.otherFrontImg, "others_front.jpg")
//        )
//
//        var allImagesUploaded = true
//
//        for((imageview, filename) in imageMappings){
//            val imageFile = File(applicationContext.filesDir, filename)
//            if(imageFile.exists()){
//                imageview.setImageURI(Uri.fromFile(imageFile))
//            }else{
//                allImagesUploaded = false
//            }
//        }
//
//        binding.uploadDocBtn.isEnabled = !allImagesUploaded
//        binding.uploadDocBtn.visibility = if(allImagesUploaded) View.GONE else View.VISIBLE
//        binding.completeBtn.visibility = if(allImagesUploaded) View.VISIBLE else View.GONE
//        binding.goBackBtn1.visibility = if(allImagesUploaded) View.VISIBLE else View.GONE
//    }

}