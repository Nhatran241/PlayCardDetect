package com.machinelearning.playcarddetect.modules.accessibilityaction

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

open class BaseActionService : AccessibilityService() {

    enum class Response{
        COMPLETED,FAILED,ACTIONNULL
    }
    companion object{
        const val TAG = "accessibility"
    }
    override fun onAccessibilityEvent(p0: AccessibilityEvent?) {
    }

    override fun onInterrupt() {
    }

    fun performAction(actions: MutableList<Action>, callback: ((ActionResponse) -> Unit)) {
        if (actions.isEmpty()) {
            callback.invoke(ActionResponse.COMPLETED)
        } else {
                val action = actions.first()
                Observable.timer(action.delayTime, TimeUnit.MILLISECONDS).doOnComplete {
                    if(action is GestureAction){
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
                        val launchIntent = packageManager.getLaunchIntentForPackage(action.packageName)
                        if(launchIntent!=null){
                            startActivity(launchIntent)
                            actions.removeFirst()
                            performAction(actions, callback)
                        }else{
                            callback.invoke(ActionResponse.FAILED)
                        }
                    }
                }.observeOn(Schedulers.io()).subscribe()
            }
    }
}