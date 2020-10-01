package com.machinelearning.playcarddetect.modules.admin.presenter

import android.graphics.RectF
import com.machinelearning.playcarddetect.common.BaseActivity
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.Verify
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.client.DeviceState
import com.machinelearning.playcarddetect.modules.client.DeviceStateBundle

fun BaseActivity.getNextAction(severData : Map<String,DeviceStateBundle>, commingDeviceID : String, commingDeviceStateBundle: DeviceStateBundle):Action{
    /**
     * Những action không cần ràng buộc điều kiện
     */
    when(commingDeviceStateBundle.deviceState){
        DeviceState.NOTDETECTED ->{
            /**
             * Device mới connect đến server chưa biết trạng thái hiện tại của device
             * Ở đây nên check device hiện tại đang ở đâu hoặc clear toàn bộ ứng dụng đang chạy và mở game lên
             */
            return OpenAppAction(100,Cons.OpenAppActionType,"gsn.game.zingplaynew1")
        }
        DeviceState.DEVICE_OPENAPP_COMPLETED ->{
            /**
             * Device mở app thành công || package game tồn tại
             * ở đây có thể phải thông qua 1 bước đăng nhập tài khoản để vào màn hình game
             */
            return OpenGameMenuAction(100,Cons.OpenGameMenuActionType)
        }
        DeviceState.DEVICE_OPENAPP_FAILED ->{
            /**
             * Packagename không tồn tại trong device
             * yêu cầu device phải tải game về trước khi sử dụng auto
             */
            return Action(0,Cons.EmptyActionType)
        }
        /**
         * Mở màn hình chọn bàn thành công -> Click vào button tạo bàn | Xác nhận = text "Dong y"
         */
        DeviceState.DEVICE_OPENCHONBANSCREEN_COMPLETED ->{
            return ClickActionWithVerify(RectF(336f,26f,336f,26f),0,50,500,Cons.OpenTaoBanActionType,
                    Verify(RectF(294f,348f,423f,392f),"Dong y"))
        }
        /**
         * Mở màn hình tạo bàn thành công -> Click vào button đồng ý | Xác nhận = màn hình trong bàn có số bàn vv
         */
        DeviceState.DEVICE_OPENTAOBANSCREEN_COMPLETED ->{
            return ClickActionWithVerify(RectF(358f,372f,358f,372f),0,50,500,Cons.ClickDongYTaoBanActionType,
                    Verify(RectF(55f,5f,182f,95f),"Cuoc,Ban,Choi,qua,phut,moi,ngay,se,anh,huong,xau,toi,suc,khoe"))
        }
        /**
         * Device đã tạo bàn thành công -> yêu cầu Admin chọn ngẫu nhiên 2 device khác để join bàn
         */
        DeviceState.DEVICE_CREATEROOM_COMPLETED ->{
            return ForceClientJoinRoom(0,Cons.JoinRoomActionType,"301")
        }
        /**
         * Join phòng thành công -> đợi chủ phòng bắt đầu game
         */
        DeviceState.DEVICE_JOINROOM_COMPLETED ->{
            return Action(0,Cons.EmptyActionType)
        }
    }
    /**
     * Những action có ràng buộc điều kiện
     */
    var numberClientConnected = severData.keys.size
    if(numberClientConnected < 3){

    }else {
        var numberClientOpenMenuGameCompleted = 0;
        severData.keys.forEach {
            if(severData[it]?.deviceState == DeviceState.DEVICE_OPENGAMEMENU_COMPLETED)
                numberClientOpenMenuGameCompleted++
        }
        if(numberClientOpenMenuGameCompleted >=2 && commingDeviceStateBundle.deviceState == DeviceState.DEVICE_OPENGAMEMENU_COMPLETED){
            /**
             * Có it nhất 2 device đã mở game thành công và devive hiện tại vừa connected đến cũng đang mở game thành công
             * ta sẽ yêu cầu device mới connect đến tạo phòng
             */
            return ClickActionWithVerify(RectF(595f,203f,595f,203f),0,100,500,Cons.OpenChonBanActionType,
                    Verify(RectF(70f,8f,211f,41f),"Chon ban"))
        }
        // Đã có ít nhất 3 device mở game thành công ở đây sẽ tiến hành tạo phòng và yêu cầu các device khác join vào phòng
        return Action(0,Cons.EmptyActionType)
    }
    return Action(0,Cons.EmptyActionType)
}
fun BaseActivity.getNextClientAction(adminAction: AdminAction,severData: Map<String, DeviceStateBundle>,commingDeviceID: String,commingDeviceStateBundle: DeviceStateBundle):HashMap<String,Action>{
    var mapAction = hashMapOf<String,Action>()
    when(adminAction){
        is ForceClientJoinRoom -> {
            var numberClientJoinRoomSuccess = 0
            var listClientReadyToJoin = mutableListOf<String>()
            severData.keys.forEach {
                if(severData[it]?.deviceState == DeviceState.DEVICE_JOINROOM_COMPLETED&&
                        severData[it]?.message==adminAction.roomNumber)
                    numberClientJoinRoomSuccess++
                if(severData[it]?.deviceState == DeviceState.DEVICE_OPENGAMEMENU_COMPLETED)
                    listClientReadyToJoin.add(it)
            }
            var numberClientWillJoin =  2-numberClientJoinRoomSuccess
            if(numberClientWillJoin >0&&listClientReadyToJoin.size >= numberClientWillJoin){
                while (mapAction.size<numberClientWillJoin) {
                    mapAction.put(listClientReadyToJoin.first(),JoinRoomAction(0,Cons.JoinRoomActionType,adminAction.roomNumber))
                    listClientReadyToJoin.removeFirst()
                }
                return mapAction
            }else{
                /**
                 * Không đủ client để join nên hủy room
                 */
                mapAction.put(commingDeviceID,DestroyRoomAction(0,Cons.DestroyRoomActionType))
                return mapAction
            }

        }
    }
    return mapAction
}
