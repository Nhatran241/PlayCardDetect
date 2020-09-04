package com.machinelearning.playcarddetect.modules.client.service

import com.machinelearning.playcarddetect.modules.accessibilityaction.BaseActionService
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.client.DeviceStats

fun BaseActionService.mappingDeviceStats(action :Action,actionResponse: ActionResponse):DeviceStats{
    when(action){
        is OpenAppAction -> {
            if(actionResponse == ActionResponse.COMPLETED) return DeviceStats.DEVICE_OPENAPP_COMPLETED else DeviceStats.DEVICE_OPENAPP_FAILED
        }
        is CaptureScreenAction ->{
            if(actionResponse == ActionResponse.COMPLETED) return DeviceStats.DEVICE_STARTCAPTURE_COMPLETED else DeviceStats.DEVICE_STARTCAPTURE_FAILED
        }
        is OpenGameMenuAction ->{
            if(actionResponse == ActionResponse.COMPLETED) return DeviceStats.DEVICE_OPENGAMEMENU_COMPLETED else DeviceStats.DEVICE_OPENGAMEMENU_FAILED
        }
        is OpenChonBanScreenAction ->{
            if(actionResponse == ActionResponse.COMPLETED) return DeviceStats.DEVICE_OPENCHONBANSCREEN_COMPLETED else DeviceStats.DEIVCE_OPENCHONBANSCREEN_FAILED
        }
    }
    return DeviceStats.NOTDETECTED
}