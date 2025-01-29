package com.example.testui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.example.testui.LoadingAnim
import com.example.testui.MainActivity
import com.example.testui.R
import com.example.testui.databinding.ActivityOtpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OtpActivity : LoadingAnim() {

    private lateinit var binding:ActivityOtpBinding
    private lateinit var auth:FirebaseAuth
    private var verificationId: String?= null

    private lateinit var resendCodeTxt:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.otp_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        otpFocus()

        val submitBtn = binding.submitBtn
        val detailTxt = binding.detailTxt1
        resendCodeTxt = binding.newCodeTxt

        val number = intent.getStringExtra("number")
        verificationId = intent.getStringExtra("verificationId")

        detailTxt.text = "Please, enter code from SMS \nwhich we sent on $number"

        val sharedPreferences = getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("number",number)
        editor.apply()

        submitBtn.setOnClickListener {

            val digit1 = binding.digit1.text.toString()
            val digit2 = binding.digit2.text.toString()
            val digit3 = binding.digit3.text.toString()
            val digit4 = binding.digit4.text.toString()
            val digit5 = binding.digit5.text.toString()
            val digit6 = binding.digit6.text.toString()

            val code = digit1 + digit2 + digit3 + digit4 + digit5 + digit6

            if(code.length == 6 && verificationId != null){
                verifyOtp(code)
                showLoadingDialog()
            }else{
                hideLoadingDialog()
                startResendCodeTimer()
                Toast.makeText(this, "Something went wrong: $code", Toast.LENGTH_SHORT).show()
            }

        }

        resendCodeTxt.isEnabled = false
        resendCodeTxt.setTextColor(getColor(R.color.disabled_color))

        resendCodeTxt.setOnClickListener {
            finish()
        }

    }

    private fun verifyOtp(otp:String){
        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this){ task ->

                hideLoadingDialog()
                if(task.isSuccessful){
                    Toast.makeText(this, "Verification Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }else{
                    startResendCodeTimer()
                    Toast.makeText(this, "Verification Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun otpFocus(){

        val digit1 = binding.digit1
        val digit2 = binding.digit2
        val digit3 = binding.digit3
        val digit4 = binding.digit4
        val digit5 = binding.digit5
        val digit6 = binding.digit6

        val editTexts = listOf(digit1, digit2, digit3, digit4, digit5, digit6)

        digit1.requestFocus()

        for(i in editTexts.indices){

            editTexts[i].addTextChangedListener {
                if(it?.length == 1 && i < editTexts.size - 1){
                    editTexts[i+1].requestFocus()

                }else if(it?.isEmpty() == true && i > 0){
                    editTexts[i - 1].requestFocus()

                }
            }
        }
    }

    private fun startResendCodeTimer(){
        object : CountDownTimer(10000,1000){
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished/1000
                resendCodeTxt.isEnabled = false
                resendCodeTxt.setTextColor(getColor(R.color.disabled_color))
                resendCodeTxt.text = "Send new code in $secondsRemaining sec"
            }

            override fun onFinish() {
                resendCodeTxt.isEnabled = true
                resendCodeTxt.text = "Send new code"
                resendCodeTxt.setTextColor(getColor(R.color.black))
            }

        }.start()
    }
}