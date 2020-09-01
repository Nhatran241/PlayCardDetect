package com.machinelearning.playcarddetect.modules.accessibilityaction.action;

 public class Action {
    public long actionTime = System.currentTimeMillis();
    public long delayTime = 50;

     public Action(int delayTime) {
         this.delayTime = delayTime;
     }

     public Action() {
     }
 }
