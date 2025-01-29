package com.example.testui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.testui.R
import com.example.testui.databinding.ActivityCheckInBinding

class CheckInActivity : AppCompatActivity() {


    private lateinit var binding: ActivityCheckInBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCheckInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val contactName = binding.contactName
        val contactNo = binding.contactNo
        val work = binding.workTxt
        val companyName = binding.companyName
        val address = binding.addressTxt
        val checkInBtn = binding.checkInBtn

        val toolbar = findViewById<Toolbar>(R.id.toolbar7)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.ic_arrow_left)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.black))

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }


        val contactNameTxt = intent.getStringExtra("contactName")
        val companyNameTxt = intent.getStringExtra("companyName")
        val addressTxt = intent.getStringExtra("address")
        val workTxt = intent.getStringExtra("workAssigned")


        contactName.text = contactNameTxt
        companyName.text = companyNameTxt
        address.text = addressTxt
        work.text = workTxt


        checkInBtn.setOnClickListener {

            val intent = Intent(this, PictureActivity::class.java)
            startActivity(intent)

        }


    }

}