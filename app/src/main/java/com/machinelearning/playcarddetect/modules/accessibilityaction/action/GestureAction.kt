package com.machinelearning.playcarddetect.modules.accessibilityaction.action

import android.accessibilityservice.GestureDescription
import com.nhatran241.accessibilityactionmodule.model.Action

open class GestureAction(var gestureDescription: GestureDescription?=null) : Action()