package com.machinelearning.playcarddetect.modules.datamanager

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.*
import java.util.*
import kotlin.collections.ArrayList


fun mappingActions(documentSnapshot: DocumentSnapshot,actionType :String):Action{
//    val actions: MutableList<Action> = ArrayList()
    when(actionType){
        Cons.SwipeActionType ->{
            val swipeAction = documentSnapshot.toObject(SwipeAction::class.java)
            if (swipeAction!=null) {
                swipeAction.path.moveTo(swipeAction.swipeStartRectF.centerX(), swipeAction.swipeStartRectF.centerY())
                swipeAction.path.lineTo(swipeAction.swipeEndRectF.centerX(), swipeAction.swipeEndRectF.centerY())
//                actions.add(swipeAction)
                return swipeAction
            }
        }
        Cons.ClickActionType ->{
            val clickAction = documentSnapshot.toObject(ClickAction::class.java)
            if(clickAction!=null) {
                clickAction.path.moveTo(clickAction.clickRectF.centerX(), clickAction.clickRectF.centerY())
//                actions.add(clickAction)
                return clickAction
            }
        }
        Cons.OpenAppActionType ->{
            val openAppAction = documentSnapshot.toObject(OpenApp::class.java)
            if(openAppAction!=null) {
//                actions.add(openAppAction)
                return openAppAction
            }
        }
        Cons.MutlpleGestureActionType -> {
            val multipleGestureAction = documentSnapshot.toObject(MultipleGestureAction::class.java)
//            for (gestureAction in multipleGestureAction!!.gestureActionList) {
//                when (gestureAction.actionType) {
//                    Cons.ClickActionType -> {
//                        Log.d("mappingAction", "mappingActions: $gestureAction")
////                        gestureAction.path.moveTo(gestureAction.clickRectF.centerX(), gestureAction.clickRectF.centerY())
//                    }
//                    Cons.SwipeActionType -> {
//                        gestureAction as SwipeAction
//                        gestureAction.path.moveTo(gestureAction.swipeStartRectF.centerX(), gestureAction.swipeStartRectF.centerY())
//                        gestureAction.path.lineTo(gestureAction.swipeEndRectF.centerX(), gestureAction.swipeEndRectF.centerY())
//                    }
//                }
//                actions.add(gestureAction)
//            }
        }
    }
    return Action(0,Cons.EmptyActionType)
}

