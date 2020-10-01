package com.machinelearning.playcarddetect.modules.client;

public class DeviceStateBundle {
    public DeviceState deviceState;
    public String message;
    public Long timeStamp;

    public DeviceStateBundle(DeviceState deviceState, String message) {
        this.deviceState = deviceState;
        this.message = message;
        this.timeStamp = System.currentTimeMillis();
    }
    public DeviceStateBundle(){

    }
    public DeviceState getDeviceState() {
        return deviceState;
    }

    public void setDeviceState(DeviceState deviceState) {
        this.deviceState = deviceState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
