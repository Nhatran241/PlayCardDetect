package com.machinelearning.playcarddetect.modules.client.service

import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.machinelearning.playcarddetect.common.Cons.STARTSERVICE
import com.machinelearning.playcarddetect.common.model.CardBase64
import com.machinelearning.playcarddetect.common.setNotification
import com.machinelearning.playcarddetect.modules.datamanager.CaptureManager
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager
import com.machinelearning.playcarddetect.modules.datamanager.TextCollectionManager.CurrentPosition
import com.machinelearning.playcarddetect.modules.accessibilityaction.BaseActionService
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import com.machinelearning.playcarddetect.modules.client.DeviceStats
import com.machinelearning.playcarddetect.modules.datamanager.TextCollectionManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit


class ClientResponseDataService : BaseActionService(),ServerClientDataManager.IClientPushDeviceStatsCallback{
    companion object{
        var isConnected = false
        const val TAG ="acessibilityService"
    }

    private var isOpenGameMenuAction: Boolean = false
    private var openGameMenuActionCallback: ((ActionResponse) -> Unit)? = null
    private var mHandler: Handler? = null
    private var captureManager: CaptureManager? = null


    /**
     * Data
     */
    private val listCardInHand: MutableList<CardBase64> = ArrayList()
    private var scaleRatio = 0f
    private var newHeight = 0
    private var newWidth = 0
    private var screenWidth = 0
    private var screenHeight = 0
    private var currentPosition = CurrentPosition.Undetected
    private var numberRoomRect = Rect(113, 36, 147, 71)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent!=null){
            if(intent.action==STARTSERVICE){
                Log.d(TAG, "onStartCommand: ")
                val display = (getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
                val size = Point()
                display.getRealSize(size)
                //            if(width<height) {
                if (size.x < size.y) {
                    screenWidth = size.y
                    screenHeight = size.x
                } else {
                    screenWidth = size.x
                    screenHeight = size.y
                }

                captureManager = CaptureManager.getInstance()
                startCapture()
                /**
                 * Register Self device to server too handle remote event
                 */
//                var click = ClickAction(RectF(1400f,300f,1400f,300f),0,100,5000,Cons.ClickActionType)
//                click.path.moveTo(click.clickRectF.centerX(),click.clickRectF.centerY())
//                performAction(mutableListOf(click)){
//
//                }
                ServerClientDataManager.getInstance().ClientPushDeviceStats(DeviceStats.NOTDETECTED){ actionResponse, deviceStats ->
                    ServerClientDataManager.getInstance().ClientListenerToRemotePath { action ->
                        Log.d(TAG, "onSuccess: " + action.actionType)
                        performAction(mutableListOf(action)) { response ->
                            ServerClientDataManager.getInstance().ClientPushDeviceStats(mappingDeviceStats(action, response),this)
                        }
                    }
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    override fun onResponse(actionResponse: ActionResponse?, deviceStats: DeviceStats?) {
//        when(deviceStats){
//            DeviceStats.NOTDETECTED ->{
//
//            }
//        }

    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        setNotification()
        isConnected = true
    }

    private fun startCapture() {
        captureManager?.setListener {
            handleBitmap(it)
        }
    }

    private fun handleBitmap(it: Bitmap?) {
        if(isOpenGameMenuAction){
            TextCollectionManager.getInstance().getTextFromBitmap(it, object : TextCollectionManager.IGetTextListener {
                override fun onGetTextSuccess(text: String?) {
                    Log.d(TAG, "onGetTextSuccess: $text")
                    if (text != null) {
                        var text = text.toLowerCase()
                        if (text.contains("bang xep hang") ||
                                text.contains("chon ban") ||
                                text.contains("mini") ||
                                text.contains("vip")) {
                            openGameMenuActionCallback?.invoke(ActionResponse.COMPLETED)
                            isOpenGameMenuAction=false
                            openGameMenuActionCallback=null
                        }
                    }
                    captureManager?.takeScreenshot()
                }

                override fun onGetTextFailed(error: String?) {
                    openGameMenuActionCallback?.invoke(ActionResponse.FAILED)
                    isOpenGameMenuAction=false
                    openGameMenuActionCallback=null
                    captureManager?.takeScreenshot()
                }

            })
        }else{
            captureManager!!.takeScreenshot()
        }
//            captureManager!!.takeScreenshot()
    }

    override fun performAction(actions: MutableList<Action>, callback: (ActionResponse) -> Unit) {
            if (actions.isEmpty()) {
                callback.invoke(ActionResponse.COMPLETED)
            } else {
                val action = actions.first()
                Observable.timer(action.delayTime, TimeUnit.MILLISECONDS).doOnComplete {
                    if(action is GestureAction){
                        Log.d(TAG, "performAction: gesture")
                        val builder = GestureDescription.Builder()
                        builder.addStroke(GestureDescription.StrokeDescription(action.path,action.startTime,action.durationTime))
                        var gestureDescription=builder.build()
                        dispatchGesture(gestureDescription, object : GestureResultCallback() {
                            override fun onCompleted(gestureDescription: GestureDescription) {
                                super.onCompleted(gestureDescription)
                                actions.removeFirst()
                                performAction(actions, callback)
                            }
                            override fun onCancelled(gestureDescription: GestureDescription) {
                                super.onCancelled(gestureDescription)
                                callback.invoke(ActionResponse.FAILED)
                            }
                        }, null)
                    }else if(action is OpenAppAction){
                        Log.d(TAG, "performAction: openapp")
                        val launchIntent = packageManager.getLaunchIntentForPackage(action.packageName)
                        if(launchIntent!=null){
                            startActivity(launchIntent)
                            actions.removeFirst()
                            performAction(actions, callback)
                        }else{
                            callback.invoke(ActionResponse.FAILED)
                        }
                    }else if(action is CaptureScreenAction){
                        Log.d(TAG, "performAction: capturescreen")
//                        startCapture()
                        actions.removeFirst()
                        performAction(actions, callback)
                    }else if(action is OpenGameMenuAction){
                        Log.d(TAG, "performAction: opengame")
                        openGameMenuActionCallback = object :(ActionResponse) -> Unit {
                            override fun invoke(p1: ActionResponse) {
                                if(p1 == ActionResponse.COMPLETED){
                                    actions.removeFirst()
                                    performAction(actions, callback)
                                }else{
                                    callback.invoke(ActionResponse.FAILED)
                                }
                            }

                        }
                        isOpenGameMenuAction=true
                    }else {
                        Log.d(TAG, "performAction: empty")
                        actions.removeFirst()
                        performAction(actions, callback)
                    }
                }.observeOn(Schedulers.io()).subscribe()
            }
    }



}