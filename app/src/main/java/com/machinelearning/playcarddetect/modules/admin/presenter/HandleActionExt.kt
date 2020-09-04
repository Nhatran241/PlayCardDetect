package com.machinelearning.playcarddetect.modules.admin.presenter

import android.bluetooth.BluetoothClass
import android.graphics.RectF
import android.util.Log
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.client.DeviceStats

fun BaseActivity.handleClientActionWithDeviceStats(deviceStats : DeviceStats):Action{
    when(deviceStats){
        DeviceStats.NOTDETECTED ->{
            return OpenAppAction(0,Cons.OpenAppActionType,"gsn.game.zingplaynew1")
        }
        DeviceStats.DEVICE_OPENAPP_COMPLETED ->{
            return CaptureScreenAction(0,Cons.CaptureScreenActionType)
        }
        DeviceStats.DEVICE_STARTCAPTURE_COMPLETED ->{
            return OpenGameMenuAction(0,Cons.OpenGameMenuActionType)
        }
        DeviceStats.DEVICE_OPENGAMEMENU_COMPLETED ->{
            return OpenChonBanScreenAction(RectF(1400f,300f,1400f,300f),0,100,500,Cons.OpenChonBanActionType)
        }
    }
    return Action(0, Cons.EmptyActionType)
}
fun BaseActivity.handleAdminActionWithResponse(actionType: String,actionResponse: ActionResponse):Action{
    return Action(0, Cons.EmptyActionType)
}
fun BaseActivity.handleClientActionWithResponse(actionType: String,actionResponse: ActionResponse):Action{
    when(actionType){
        Cons.OpenAppActionType ->{
            if(actionResponse == ActionResponse.COMPLETED){
                return OpenGameMenuAction(3000,Cons.OpenGameMenuActionType)
            }else{

            }
        }
    }
    return Action(0, Cons.EmptyActionType)
}