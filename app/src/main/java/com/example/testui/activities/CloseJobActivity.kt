package com.example.testui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testui.LoadingAnim
import com.example.testui.MainActivity
import com.example.testui.R
import com.example.testui.databinding.ActivityCloseJobBinding
import com.google.firebase.firestore.FirebaseFirestore

class CloseJobActivity : LoadingAnim() {

    private lateinit var binding: ActivityCloseJobBinding
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCloseJobBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firestore = FirebaseFirestore.getInstance()

        val sharedPreferences = getSharedPreferences("job_data", Context.MODE_PRIVATE)
        val jobId = sharedPreferences.getString("jobId","Null")

        val closeJobBtn = binding.closeJobBtn

        closeJobBtn.setOnClickListener {
            if (jobId != null) {

                showLoadingDialog()
                firestore.collection("jobs_data").whereEqualTo("jobId",jobId).get()

                    .addOnSuccessListener { querySnapshot ->
                        if(!querySnapshot.isEmpty){
                            val document = querySnapshot.documents[0]
                            val documentId = document.id

                            hideLoadingDialog()
                            firestore.collection("jobs_data")
                                .document(documentId)
                                .update("jobStatus","Closed")
                                .addOnSuccessListener {

                                    Log.d("Firestore", "Job status updated successfully")

                                    val intent = Intent(this@CloseJobActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("Firestore", "Error updating Job status: ${exception.localizedMessage}")
                                }
                        }else{
                            hideLoadingDialog()
                            Log.d("Firestore", "No document found with JobId: $jobId")
                        }
                    }
                    .addOnFailureListener {
                        hideLoadingDialog()
                        Log.d("Firestore", "Error querying firestore")
                    }
            }
        }

    }


}