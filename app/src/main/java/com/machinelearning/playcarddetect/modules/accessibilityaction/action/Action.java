package com.machinelearning.playcarddetect.modules.accessibilityaction.action;

 public class Action {
    public long actionTime = System.currentTimeMillis();
    public long delayTime = 50;
    public String actionType = "";

     public Action(int delayTime,String actionType) {
         this.delayTime = delayTime;
         this.actionType = actionType;
     }
     public Action() {
     }
 }
