package com.machinelearning.playcarddetect.modules.admin.presenter

import android.bluetooth.BluetoothClass
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.Action
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.OpenAppAction
import com.machinelearning.playcarddetect.modules.client.DeviceStats

fun BaseActivity.handleAction(deviceStats : DeviceStats):Action{
    if(deviceStats == DeviceStats.NOTDETECTED){
        return OpenAppAction(0,Cons.OpenAppActionType,"gsn.game.zingplaynew1")
    }
    return Action(0, Cons.EmptyActionType)
}