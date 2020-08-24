package com.machinelearning.playcarddetect.modules.admin.presenter

import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.machinelearning.playcarddetect.R
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.common.model.CardBase64
import com.machinelearning.playcarddetect.databinding.ActivitiyAdminBinding
import com.machinelearning.playcarddetect.modules.admin.business.AdminActivityViewModel
import com.machinelearning.playcarddetect.modules.admin.business.ItemCardViewModel
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager.*

class AdminActivity :BaseActivity(){
    lateinit var binding : ActivitiyAdminBinding
    var viewModel = AdminActivityViewModel()
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
        val serverClientDataManager = ServerClientDataManager.getInstance()
        serverClientDataManager.RegisterAdminToServer { roomID, listDeviceID ->
            Log.d(TAG, "PrepareServer: $roomID : $listDeviceID")
            if(listDeviceID.size>0) {
                dismisDialogLoading()
                listDeviceID.forEachIndexed { index, id ->
                    serverClientDataManager.RequestClientData(id) { cardBase64List, response ->
                        Log.d(TAG, "PrepareServer: requestClientData $response")
                        if (response == RESPONSE_SUCCESS) {
                            viewModel.listCard.value = cardBase64List.map {
                                ItemCardViewModel(CardBase64.convert(it.cardBitmap64),it.cardRect)
                            }
                        }
                    }
                }
            }else{
                Log.d(TAG, "PrepareServer: no room")
            }
        }

    }

}