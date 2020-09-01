package com.machinelearning.playcarddetect.modules.accessibilityaction.action;


import android.graphics.Path;
import android.graphics.RectF;

public class SwipeAction extends Action {
    long durationTime = 10;
    long startTime = 0;
    RectF startRectF = new RectF();
    RectF endRectF = new RectF();
    private Path path = new Path();
    public SwipeAction(RectF startRectF,RectF endRectF,long startTime,long durationTime, int delayTime) {
        super(delayTime);
        this.durationTime = durationTime;
        this.startTime = startTime;
        this.startRectF =startRectF;
        this.endRectF = endRectF;
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

    public RectF getStartRectF() {
        return startRectF;
    }

    public void setStartRectF(RectF startRectF) {
        this.startRectF = startRectF;
    }

    public RectF getEndRectF() {
        return endRectF;
    }

    public void setEndRectF(RectF endRectF) {
        this.endRectF = endRectF;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
