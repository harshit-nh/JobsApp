package com.example.testui.bottom_fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.Toolbar
import com.example.testui.MainActivity
import com.example.testui.R
import com.example.testui.models.JobData
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


open class AddSelfJobFragment : Fragment() {

    private lateinit var loadingDialog: Dialog

    private lateinit var startLocationLayout: TextInputLayout
    private lateinit var endLocationLayout: TextInputLayout
    private lateinit var companyNameLayout: TextInputLayout
    private lateinit var nameLayout: TextInputLayout
    private lateinit var noLayout: TextInputLayout
    private lateinit var remarkLayout: TextInputLayout

    private lateinit var startLocationTxt: TextInputEditText
    private lateinit var endLocationTxt: TextInputEditText
    private lateinit var companyNameTxt: TextInputEditText
    private lateinit var nameTxt: TextInputEditText
    private lateinit var noTxt: TextInputEditText
    private lateinit var remarkTxt: TextInputEditText

    private lateinit var createJobBtn: MaterialButton

    private lateinit var firestore: FirebaseFirestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_self_job, container, false)

        firestore = FirebaseFirestore.getInstance()

        initializeLoadingDialog()

        startLocationLayout = view.findViewById(R.id.startLocationLayout)
        endLocationLayout = view.findViewById(R.id.endLocationLayout)
        companyNameLayout = view.findViewById(R.id.companyNameLayout)
        nameLayout = view.findViewById(R.id.clientNameLayout)
        noLayout = view.findViewById(R.id.clientNoLayout)
        remarkLayout = view.findViewById(R.id.remarkLayout)

        startLocationTxt = view.findViewById(R.id.startLocationTxt)
        endLocationTxt = view.findViewById(R.id.endLocationTxt)
        companyNameTxt = view.findViewById(R.id.companyNameTxt1)
        nameTxt = view.findViewById(R.id.clientNameTxt)
        noTxt = view.findViewById(R.id.clientNoTxt)
        remarkTxt = view.findViewById(R.id.remarkTxt)


        createJobBtn = view.findViewById(R.id.createJob)



        createJobBtn.setOnClickListener {

            if(startLocationTxt.text.toString().trim().isEmpty()
                || endLocationTxt.text.toString().trim().isEmpty()
                || companyNameTxt.text.toString().trim().isEmpty()
                || nameTxt.text.toString().trim().isEmpty()
                || noTxt.text.toString().trim().isEmpty()
                || remarkTxt.text.toString().trim().isEmpty())
            {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }else{
                addJobToFirestore()
            }

        }


        return view
    }



    private fun addJobToFirestore(){

        showLoadingDialog()

//        val startLocation = startLocationTxt.text.toString().trim()
        val endLocation = endLocationTxt.text.toString().trim()
        val companyName = companyNameTxt.text.toString().trim()
        val clientName = nameTxt.text.toString().trim()
        val clientNo = noTxt.text.toString().trim()
        val remark = remarkTxt.text.toString().trim()
        

        //Fetching the highest jobId from firestore
        firestore.collection("jobs_data")
            .orderBy("jobId", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->

                var newJobId = 1 // default jobId if no job exists
                if(!documents.isEmpty){
                    val lastJob = documents.documents[0]
                    val lastJobId = lastJob.getString("jobId")?.toIntOrNull()?: 0
                    newJobId = (lastJobId + 1)
                }

                //saving the new job to firestore
                saveJobToFirestore(newJobId.toString(), endLocation, companyName, clientName, clientNo, remark)
            }
            .addOnFailureListener { e ->
                hideLoadingDialog()
                Toast.makeText(requireContext(), "Please try again", Toast.LENGTH_SHORT).show()
                Log.e("AddJob", "Error: ${e.message}")
            }
        
    }

    private fun saveJobToFirestore(jobId: String, address: String, companyName: String, clientName: String, clientNo: String, workAssigned: String) {

        val createdAt = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date())

        val jobData = JobData(
            jobId = jobId,
            address = address,
            companyName = companyName,
            contactName = clientName,
            contactNumber = clientNo,
            createdAt = createdAt,
            workAssigned = workAssigned,
            jobStatus = "Current",
            progressState = "",
            distance = "10 Km away"

        )

        firestore.collection("jobs_data")
            .add(jobData)
            .addOnSuccessListener {
                hideLoadingDialog()
                Toast.makeText(requireContext(), "Job added successfully", Toast.LENGTH_SHORT).show()

                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                hideLoadingDialog()
                Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
                Log.e("AddJob", "Error: ${e.message}")
            }


    }

    private fun initializeLoadingDialog(){
        loadingDialog = Dialog(requireContext())
        loadingDialog.setContentView(R.layout.loading_dialog)
        loadingDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loadingDialog.setCancelable(false)
    }

    protected fun showLoadingDialog(){
        if (!loadingDialog.isShowing) {
            loadingDialog.show()
        }
    }

    protected fun hideLoadingDialog(){
        if (loadingDialog.isShowing) {
            loadingDialog.hide()
        }
    }




}