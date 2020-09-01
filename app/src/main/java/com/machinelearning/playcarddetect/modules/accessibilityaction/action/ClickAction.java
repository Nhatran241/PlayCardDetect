package com.machinelearning.playcarddetect.modules.accessibilityaction.action;


import android.graphics.Path;
import android.graphics.RectF;

public class ClickAction extends Action {
    long durationTime = 10;
    long startTime = 0;
    RectF clickRectF = new RectF();
    private Path path = new Path();
    public ClickAction(RectF clickRectF,long startTime, long durationTime, int delayTime) {
        super(delayTime);
        this.durationTime = durationTime;
        this.startTime = startTime;
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

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
