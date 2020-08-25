package com.machinelearning.playcarddetect.modules.admin.presenter

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.machinelearning.playcarddetect.R
import com.machinelearning.playcarddetect.common.Action
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.common.model.CardBase64
import com.machinelearning.playcarddetect.databinding.ActivitiyAdminBinding
import com.machinelearning.playcarddetect.modules.admin.business.AdminActivityViewModel
import com.machinelearning.playcarddetect.modules.admin.business.ItemCardViewModel
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager.*
import kotlin.math.log

class AdminActivity:BaseActivity(),IAdminPutRemoteCallback,IAdminListenerToDataPath,IAdminListenerToRoomPath{
    lateinit var binding : ActivitiyAdminBinding
    var viewModel = AdminActivityViewModel()
    val serverClientDataManager =  ServerClientDataManager.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activitiy_admin)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
    }
    companion object{
        val TAG ="adminactivitylog"
    }

    override fun PrepareServer() {
        showDialogLoading()
        serverClientDataManager.AdminListenerToDataPath(this)
        serverClientDataManager.AdminListenerToRoomPath(this)
    }

    override fun onDataResponse(data: String?, message: String?) {
        Log.d(TAG, "onDataResponse: $data/$message")
    }

    override fun onRoom(data: String?, mesaage: String?) {
        Log.d(TAG, "onRoom: $data/$mesaage")
    }

    override fun onPushSuccess() {
        Log.d(TAG, "onPushSuccess: ")
    }

    override fun onPushFailed(error: String?) {
        Log.d(TAG, "onPushFailed: $error")
    }


}