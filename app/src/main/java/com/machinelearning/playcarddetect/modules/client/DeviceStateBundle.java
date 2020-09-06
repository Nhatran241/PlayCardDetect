package com.machinelearning.playcarddetect.modules.client;

public class DeviceStateBundle {
    public DeviceState deviceState;
    public String message;

    public DeviceStateBundle(DeviceState deviceState, String message) {
        this.deviceState = deviceState;
        this.message = message;
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
}
