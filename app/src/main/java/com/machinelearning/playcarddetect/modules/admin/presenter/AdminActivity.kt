package com.machinelearning.playcarddetect.modules.admin.presenter

import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.machinelearning.playcarddetect.R
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.databinding.ActivitiyAdminBinding
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.admin.business.AdminActivityViewModel
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager.*

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
//            serverClientDataManager.AdminPushRemote(ClickAction(RectF(500f,500f,500f,500f), 0,0,5000), "a", this)
//            var click1 = ClickAction(RectF(500f,500f,500f,500f),0,0,3000)
//            var click2 = ClickAction(RectF(500f,500f,500f,500f),0,0,3000)
//            var multiple = MultipleGestureAction(0, listOf(click1,click2), Cons.MutlpleGestureActionType)
              var packageName = OpenApp(3000,Cons.OpenAppActionType,"com.android.chrome")
            serverClientDataManager.AdminPushRemote(packageName,"a",this)
        }
    }

    override fun onAdminPutRemoteResponse(actionResponse: ActionResponse?, deviceId: String?) {
        if (actionResponse != null) {
            Log.d(TAG, "onAdminPutRemoteResponse: "+actionResponse.name+"/"+deviceId)
        }

    }


}