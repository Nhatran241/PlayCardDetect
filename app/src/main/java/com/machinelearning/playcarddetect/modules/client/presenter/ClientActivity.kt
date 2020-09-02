package com.machinelearning.playcarddetect.modules.client.presenter

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.common.Cons
import com.machinelearning.playcarddetect.modules.client.service.ClientResponseDataService
import com.machinelearning.playcarddetect.modules.client.service.ClientResponseDataService.Companion.isConnected
import com.machinelearning.playcarddetect.modules.datamanager.CaptureManager
import com.machinelearning.playcarddetect.modules.datamanager.CaptureManager.onGrantedPermissionListener

class ClientActivity :BaseActivity(){
    companion object{
        const val REQUESTACCESSIBILITY = 2222
        const val REQUESTCAPTURE = 2223
    }
    var  captureManager : CaptureManager =CaptureManager.getInstance()

    override fun PrepareServer() {
        askPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    private fun askPermission() {
        if (ClientResponseDataService.isConnected) {
            requestCapture()
        } else {
            askAccessibilityPermission()
        }
    }

    private fun askAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivityForResult(intent, REQUESTACCESSIBILITY)
    }



    private fun requestCapture() {
        captureManager.requestScreenshotPermission(this, REQUESTCAPTURE)
        captureManager.setOnGrantedPermissionListener { isGranted ->
            if (isGranted) {
                captureManager.init(this@ClientActivity)
                val intent = Intent(this@ClientActivity, ClientResponseDataService::class.java)
                intent.action = Cons.STARTSERVICE
                startService(intent)
            } else {
                captureManager.requestScreenshotPermission(this@ClientActivity,REQUESTCAPTURE)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUESTACCESSIBILITY) {
            if (isConnected) {
                requestCapture()
            } else {
                Toast.makeText(this, "Accessibility Service not running . Try again or reset phone", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == REQUESTCAPTURE) {
            captureManager.onActivityResult(resultCode, data)
        }
    }


}