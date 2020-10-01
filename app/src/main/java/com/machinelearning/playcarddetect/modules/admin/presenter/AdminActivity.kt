package com.machinelearning.playcarddetect.modules.admin.presenter

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.machinelearning.playcarddetect.R
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.databinding.ActivitiyAdminBinding
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.admin.business.AdminActivityViewModel
import com.machinelearning.playcarddetect.modules.client.DeviceState
import com.machinelearning.playcarddetect.modules.client.DeviceStateBundle
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager.*

class AdminActivity:BaseActivity(),IAdminListenerToDataPath,IAdminListenerToDeviceStatsPath{
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
        serverClientDataManager.AdminListenerToDeviceStatsPath(this)
     }

    override fun onDataResponse(data: String?, message: String?) {
        Log.d(TAG, "onDataResponse: $data/$message")
    }

    override fun onDeviceStatsReponse(data: MutableMap<String, DeviceStateBundle>, newDataChange: MutableMap<String, DeviceStateBundle>) {
        newDataChange.keys.forEach {
            val deviceStateBundle = newDataChange[it]
            if(deviceStateBundle!=null)
                serverClientDataManager.AdminPushRemote(createActionForClient(data,it,deviceStateBundle),it) { actionResponse, actionType, deviceId -> Log.d(TAG, "push action to " + deviceId + " status:" + actionResponse?.name); }
        }
    }


}