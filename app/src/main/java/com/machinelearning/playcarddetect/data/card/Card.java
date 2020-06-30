package com.machinelearning.playcarddetect.data.card;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Card {
  private Level cardLevel;
  private Suit cardsuit;
  private Rect cardRect;

    public Card(Level cardLevel, Suit cardsuit, Rect cardRect) {
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



    public Level getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(Level cardLevel) {
        this.cardLevel = cardLevel;
    }

    public Suit getCardsuit() {
        return cardsuit;
    }

    public void setCardsuit(Suit cardsuit) {
        this.cardsuit = cardsuit;
    }
}
