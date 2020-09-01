package com.machinelearning.playcarddetect.modules.accessibilityaction.action;

public class OpenApp extends Action {
    public String packageName ="";
    public OpenApp(int delayTime, String actionType,String packageName) {
        super(delayTime, actionType);
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public OpenApp() {
    }
}
