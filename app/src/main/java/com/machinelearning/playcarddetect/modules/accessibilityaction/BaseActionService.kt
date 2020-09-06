package com.machinelearning.playcarddetect.modules.accessibilityaction

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

abstract class BaseActionService : AccessibilityService() {

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

    abstract fun performAction(actions: MutableList<Action>,message : String, callback: ((ActionResponse,String) -> Unit))

}