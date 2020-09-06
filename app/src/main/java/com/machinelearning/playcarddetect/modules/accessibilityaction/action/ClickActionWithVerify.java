package com.machinelearning.playcarddetect.modules.accessibilityaction.action;


import android.graphics.RectF;

import com.machinelearning.playcarddetect.modules.accessibilityaction.Verify;

public class ClickActionWithVerify extends GestureAction {
    public Verify verify;
    RectF clickRectF = new RectF();
    public ClickActionWithVerify(RectF clickRectF, long startTime, long durationTime, int delayTime, String actionType,Verify verify) {
        super(startTime,durationTime,delayTime,actionType);
        this.clickRectF =clickRectF;
        this.verify = verify;
    }

    public ClickActionWithVerify() {
    }

    public Verify getVerify() {
        return verify;
    }

    public void setVerify(Verify verify) {
        this.verify = verify;
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
