package com.machinelearning.playcarddetect.common.model;

import java.util.List;

public class ClientModel {
    String clientID;
    String currentRoom;
    List<CardBase64> cardBase64List;


    public ClientModel(String clientID, String currentRoom, List<CardBase64> cardBase64List) {
        this.clientID = clientID;
        this.currentRoom = currentRoom;
        this.cardBase64List = cardBase64List;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(String currentRoom) {
        this.currentRoom = currentRoom;
    }

    public List<CardBase64> getCardBase64List() {
        return cardBase64List;
    }

    public void setCardBase64List(List<CardBase64> cardBase64List) {
        this.cardBase64List = cardBase64List;
    }
}
