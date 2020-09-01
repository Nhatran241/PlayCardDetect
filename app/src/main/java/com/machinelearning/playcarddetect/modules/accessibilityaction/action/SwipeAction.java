package com.machinelearning.playcarddetect.modules.accessibilityaction.action;


import android.graphics.RectF;

public class SwipeAction extends GestureAction {
    RectF swipeStartRectF = new RectF();
    RectF swipeEndRectF = new RectF();
    public SwipeAction(RectF swipeStartRectF,RectF swipeEndRectF, long startTime, long durationTime, int delayTime) {
        super(startTime,durationTime,delayTime,"Swipe");
        this.swipeStartRectF =swipeStartRectF;
        this.swipeEndRectF = swipeEndRectF;
    }

    public SwipeAction() {
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

    public RectF getSwipeStartRectF() {
        return swipeStartRectF;
    }

    public void setSwipeStartRectF(RectF swipeStartRectF) {
        this.swipeStartRectF = swipeStartRectF;
    }

    public RectF getSwipeEndRectF() {
        return swipeEndRectF;
    }

    public void setSwipeEndRectF(RectF swipeEndRectF) {
        this.swipeEndRectF = swipeEndRectF;
    }
}
