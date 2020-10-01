package com.machinelearning.playcarddetect.modules.accessibilityaction.action;

public class JoinRoomAction extends Action {
    public String numberRoom;
    public JoinRoomAction(int delayTime, String actionType,String numberRoom) {
        super(delayTime, actionType);
        this.numberRoom = numberRoom;
    }

    public JoinRoomAction() {
    }
}
