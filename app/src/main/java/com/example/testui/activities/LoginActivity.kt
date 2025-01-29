package com.example.testui.activities

import android.Manifest
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.util.Log
import android.widget.Button
import android.widget.RemoteViews
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testui.LoadingAnim
import com.example.testui.MainActivity
import com.example.testui.R
import com.example.testui.databinding.ActivityLoginBinding
import com.google.android.datatransport.Priority
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import java.util.concurrent.TimeUnit

class LoginActivity : LoadingAnim() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var phoneEditTxt: TextInputEditText
    private lateinit var getCodeBtn: MaterialButton
    private lateinit var resendTxt: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()


        if(auth.currentUser != null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

        resendTxt = binding.resendTxt
        phoneEditTxt = binding.phoneEditTxt
        getCodeBtn = binding.getCodeBtn

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
                hideLoadingDialog()
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                hideLoadingDialog()
                Toast.makeText(this@LoginActivity, "Verification failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(verificationId, token)

                hideLoadingDialog()

                val intent = Intent(this@LoginActivity, OtpActivity::class.java)
                intent.putExtra("verificationId", verificationId)
                intent.putExtra("number", phoneEditTxt.text.toString())
                startActivity(intent)
            }

        }

        getCodeBtn.setOnClickListener {

            val phoneNumber = "+91${phoneEditTxt.text.toString().trim()}"
            if(phoneNumber.length == 13){
                sendVerificationCode(phoneNumber)
                showLoadingDialog()
            }else{
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun sendVerificationCode(phoneNumber:String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->

                hideLoadingDialog()
                if(task.isSuccessful){
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
                
            }
    }

}