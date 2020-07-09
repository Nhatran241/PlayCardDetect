package com.machinelearning.playcarddetect.data.model;

import android.graphics.Rect;

public class Card {
  private String cardLevel;
  private Suit cardsuit;
  private Rect cardRect;

    public enum Suit {
        Co,
        Ro,
        Chuon,
        Bich,
        NotDetect
    }
    public Card(String cardLevel, Suit cardsuit, Rect cardRect) {
        this.cardLevel = cardLevel;
        this.cardsuit = cardsuit;
        this.cardRect = cardRect;
    }
    public Card() {
    }
    public Rect getCardRect() {
        return cardRect;
    }

    public void setCardRect(Rect cardRect) {
        this.cardRect = cardRect;
    }



    public String getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(String cardLevel) {
        this.cardLevel = cardLevel;
    }

    public Suit getCardsuit() {
        return cardsuit;
    }

    public void setCardsuit(Suit cardsuit) {
        this.cardsuit = cardsuit;
    }
}
