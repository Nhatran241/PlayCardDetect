package com.machinelearning.playcarddetect.modules.client.service

import com.machinelearning.playcarddetect.modules.accessibilityaction.BaseActionService
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.client.DeviceState
import com.machinelearning.playcarddetect.modules.client.DeviceStateBundle

fun BaseActionService.mappingDeviceStats(action :Action,actionResponse: ActionResponse,message : String):DeviceStateBundle{
    when(action){
        is OpenAppAction -> {
            return if(actionResponse == ActionResponse.COMPLETED) DeviceStateBundle(DeviceState.DEVICE_OPENAPP_COMPLETED,message) else DeviceStateBundle(DeviceState.DEVICE_OPENAPP_FAILED,message)
        }
        is CaptureScreenAction ->{
            return if(actionResponse == ActionResponse.COMPLETED) DeviceStateBundle(DeviceState.DEVICE_STARTCAPTURE_COMPLETED,message) else DeviceStateBundle(DeviceState.DEVICE_STARTCAPTURE_FAILED,message)
        }
        is OpenGameMenuAction ->{
            return if(actionResponse == ActionResponse.COMPLETED) DeviceStateBundle(DeviceState.DEVICE_OPENGAMEMENU_COMPLETED,message) else DeviceStateBundle(DeviceState.DEVICE_OPENGAMEMENU_FAILED,message)
        }
        is ClickActionWithVerify ->{
            when(action.actionType){
                Cons.OpenChonBanActionType -> {
                    return if(actionResponse == ActionResponse.COMPLETED) DeviceStateBundle(DeviceState.DEVICE_OPENCHONBANSCREEN_COMPLETED,message) else DeviceStateBundle(DeviceState.DEIVCE_OPENCHONBANSCREEN_FAILED,message)
                }
                Cons.OpenTaoBanActionType -> {
                    return if(actionResponse == ActionResponse.COMPLETED) DeviceStateBundle(DeviceState.DEVICE_OPENTAOBANSCREEN_COMPLETED,message) else DeviceStateBundle(DeviceState.DEVICE_OPENTAOBANSCREEN_FAILED,message)
                }
                Cons.ClickDongYTaoBanActionType ->{
                    return if(actionResponse == ActionResponse.COMPLETED) DeviceStateBundle(DeviceState.DEVICE_CREATEROOM_COMPLETED,message) else DeviceStateBundle(DeviceState.DEVICE_CREATEROOM_FAILED,message)
                }
            }
        }

    }
    return DeviceStateBundle(DeviceState.NOTDETECTED,message)
}