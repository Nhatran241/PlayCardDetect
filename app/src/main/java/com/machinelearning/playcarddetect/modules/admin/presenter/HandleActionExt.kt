package com.machinelearning.playcarddetect.modules.admin.presenter

import android.bluetooth.BluetoothClass
import android.util.Log
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.client.DeviceStats

fun BaseActivity.handleClientActionWithDeviceStats(deviceStats : DeviceStats):Action{
    if(deviceStats == DeviceStats.NOTDETECTED){
        return OpenAppAction(0,Cons.OpenAppActionType,"gsn.game.zingplaynew1")
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