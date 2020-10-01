package com.machinelearning.playcarddetect.modules.accessibilityaction.action;

import java.util.ArrayList;

public class ForceClientJoinRoom extends AdminAction {
    public String roomNumber;
    public ForceClientJoinRoom(int delayTime, String actionType,String roomNumber) {
        super(delayTime, actionType);
        this.roomNumber = roomNumber;
    }

    public ForceClientJoinRoom() {
    }
}
