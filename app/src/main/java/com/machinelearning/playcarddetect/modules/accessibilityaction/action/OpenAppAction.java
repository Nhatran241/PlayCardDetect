package com.machinelearning.playcarddetect.modules.accessibilityaction.action;

public class OpenAppAction extends Action {
    public String packageName ="";
    public OpenAppAction(int delayTime, String actionType, String packageName) {
        super(delayTime, actionType);
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public OpenAppAction() {
    }
}
