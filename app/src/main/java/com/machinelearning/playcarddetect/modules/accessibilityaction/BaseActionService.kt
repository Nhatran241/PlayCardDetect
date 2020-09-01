package com.machinelearning.playcarddetect.modules.accessibilityaction

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.GestureAction
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.SwipeAction
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

    fun performAction(actions: MutableList<GestureAction>, callback: ((String) -> Unit)) {
        if (actions.isEmpty()) {
            callback.invoke(Response.COMPLETED.toString())
        } else {
                val builder = GestureDescription.Builder()
                val action = actions.first()
            Log.d(TAG, "performAction: perfromAction"+action.delayTime)
            builder.addStroke(GestureDescription.StrokeDescription(action.path,action.startTime,action.durationTime))
                var gestureDescription=builder.build()
                Observable.timer(action.delayTime, TimeUnit.MILLISECONDS).doOnComplete {
                    dispatchGesture(gestureDescription, object : GestureResultCallback() {
                        override fun onCompleted(gestureDescription: GestureDescription) {
                            super.onCompleted(gestureDescription)
                            actions.removeFirst()
                            performAction(actions, callback)
                        }

                        override fun onCancelled(gestureDescription: GestureDescription) {
                            super.onCancelled(gestureDescription)
                            callback.invoke(Response.FAILED.toString())
                        }
                    }, null)
                }.observeOn(Schedulers.io()).subscribe()
        }
    }
}