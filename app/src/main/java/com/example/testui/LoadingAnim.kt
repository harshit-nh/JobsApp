package com.example.testui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity

open class LoadingAnim: AppCompatActivity() {

    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeLoadingDialog()
    }


    private fun initializeLoadingDialog(){
        loadingDialog = Dialog(this)
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