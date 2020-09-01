package com.machinelearning.playcarddetect.modules.accessibilityaction.action;

import java.util.ArrayList;
import java.util.List;

public class MultipleGestureAction extends Action {

    public MultipleGestureAction(int delayTime, List<GestureAction> gestureActionList, String actionType) {
        super(delayTime, actionType);
        this.gestureActionList = gestureActionList;
    }

    public MultipleGestureAction(){

    }

    List<GestureAction> gestureActionList = new ArrayList<>();

    public void addGestureAction(GestureAction gestureAction){
        gestureActionList.add(gestureAction);
    }
    public void removeGestureAction(int index){
        gestureActionList.remove(index);
    }
    public void removeGestureAction(GestureAction gestureAction){
        gestureActionList.remove(gestureAction);
    }

    public List<GestureAction> getGestureActionList() {
        return gestureActionList;
    }

    public void setGestureActionList(List<GestureAction> gestureActionList) {
        this.gestureActionList = gestureActionList;
    }
}
