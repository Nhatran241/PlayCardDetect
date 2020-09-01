package com.machinelearning.playcarddetect.modules.accessibilityaction.action;


import android.graphics.Path;
import android.graphics.RectF;

public class GestureAction extends Action {
    long durationTime = 10;
    long startTime = 0;
    private Path path = new Path();
    public GestureAction(long startTime, long durationTime, int delayTime,String type) {
        super(delayTime,type);
        this.durationTime = durationTime <= 0 ? 1: durationTime;
        this.startTime = startTime;
    }

    public GestureAction() {
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


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
