package com.machinelearning.playcarddetect.common;

public class Action {
    private Long actionTime = System.currentTimeMillis();

    public Long getActionTime() {
        return actionTime;
    }

    public void setActionTime(Long actionTime) {
        this.actionTime = actionTime;
    }
}
