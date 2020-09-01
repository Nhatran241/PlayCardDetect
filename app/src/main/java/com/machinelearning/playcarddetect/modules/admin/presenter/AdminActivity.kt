package com.machinelearning.playcarddetect.modules.admin.presenter

import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.machinelearning.playcarddetect.R
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.databinding.ActivitiyAdminBinding
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.ClickAction
import com.machinelearning.playcarddetect.modules.admin.business.AdminActivityViewModel
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager.*
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.SwipeAction

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
        serverClientDataManager.AdminListenerToDataPath(this)
        serverClientDataManager.AdminListenerToRoomPath(this)
     }

    override fun onDataResponse(data: String?, message: String?) {
        Log.d(TAG, "onDataResponse: $data/$message")
    }

    override fun onRoom(data: MutableMap<String, String>?, mesaage: String?) {
        Log.d(TAG, "onRoom: $data/$mesaage")
        if (data != null) {
            val entry = data.entries.iterator().next()
            val key = entry.key
            val value = entry.value
//            serverClientDataManager.AdminPushRemote(SwipeAction(RectF(500f,500f,500f,500f),
//            RectF(0f,500f,0f,500f),0,100,5000), "a", this)
            serverClientDataManager.AdminPushRemote(ClickAction(RectF(500f,500f,500f,500f), 0,0,5000), "a", this)


        }
    }

    override fun onPushSuccess() {
        Log.d(TAG, "onPushSuccess: ")
    }

    override fun onPushFailed(error: String?) {
        Log.d(TAG, "onPushFailed: $error")
    }


}