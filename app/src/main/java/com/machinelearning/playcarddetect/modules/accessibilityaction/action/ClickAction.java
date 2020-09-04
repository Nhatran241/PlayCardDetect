package com.machinelearning.playcarddetect.modules.accessibilityaction.action;


import android.graphics.Path;
import android.graphics.RectF;

public class ClickAction extends GestureAction {
    RectF clickRectF = new RectF();
    public ClickAction(RectF clickRectF,long startTime, long durationTime, int delayTime,String actionType) {
        super(startTime,durationTime,delayTime,actionType);
        this.clickRectF =clickRectF;
    }

    public ClickAction() {
    }

    public long getDurationTime() {
        return durationTime;
    }

    public void setDurationTime(long durationTime) {
        this.durationTime = durationTime;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public RectF getClickRectF() {
        return clickRectF;
    }

    public void setClickRectF(RectF clickRectF) {
        this.clickRectF = clickRectF;
    }

}
