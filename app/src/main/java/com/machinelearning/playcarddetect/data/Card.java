package com.machinelearning.playcarddetect.data;

public class Card {
    private String cardLevel ;
    private String cardSuit ;

    public Card(String cardLevel, String cardSuit) {
        this.cardLevel = cardLevel;
        this.cardSuit = cardSuit;
    }

    public String getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(String cardLevel) {
        this.cardLevel = cardLevel;
    }

    public String getCardSuit() {
        return cardSuit;
    }

    public void setCardSuit(String cardSuit) {
        this.cardSuit = cardSuit;
    }
}
