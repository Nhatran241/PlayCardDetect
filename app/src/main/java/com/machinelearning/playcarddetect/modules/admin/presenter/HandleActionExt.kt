package com.machinelearning.playcarddetect.modules.admin.presenter

import android.graphics.RectF
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.Verify
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.client.DeviceState

fun BaseActivity.handleClientActionWithDeviceStats(deviceState : DeviceState):Action{
    when(deviceState){
        DeviceState.NOTDETECTED ->{
            return OpenAppAction(0,Cons.OpenAppActionType,"gsn.game.zingplaynew1")
        }
        DeviceState.DEVICE_OPENAPP_COMPLETED ->{
            return CaptureScreenAction(0,Cons.CaptureScreenActionType)
        }
        DeviceState.DEVICE_STARTCAPTURE_COMPLETED ->{
            return OpenGameMenuAction(0,Cons.OpenGameMenuActionType)
        }
        DeviceState.DEVICE_OPENGAMEMENU_COMPLETED ->{
            return ClickActionWithVerify(RectF(595f,203f,595f,203f),0,100,500,Cons.OpenChonBanActionType,
            Verify(RectF(70f,8f,211f,41f),"Chon ban"))
        }
        DeviceState.DEVICE_OPENCHONBANSCREEN_COMPLETED ->{
            return ClickActionWithVerify(RectF(336f,26f,336f,26f),0,50,500,Cons.OpenTaoBanActionType,
                    Verify(RectF(294f,348f,423f,392f),"Dong y"))
        }
        DeviceState.DEVICE_OPENTAOBANSCREEN_COMPLETED ->{
            return ClickActionWithVerify(RectF(358f,372f,358f,372f),0,50,500,Cons.ClickDongYTaoBanActionType,
                    Verify(RectF(55f,5f,182f,95f),"Cuoc,Ban,Choi,qua,phut,moi,ngay,se,anh,huong,xau,toi,suc,khoe"))
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