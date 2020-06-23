package com.machinelearning.playcarddetect.data.card;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Level {
    private String cardLevel;
    private Rect cardLevelRect;
    private Bitmap cardLevelBitmap;

    public Bitmap getCardLevelBitmap() {
        return cardLevelBitmap;
    }

    public void setCardLevelBitmap(Bitmap cardLevelBitmap) {
        this.cardLevelBitmap = cardLevelBitmap;
    }

    public Level(String cardLevel, Rect cardLevelRect) {
        this.cardLevel = cardLevel;
        this.cardLevelRect = cardLevelRect;
    }

    public String getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(String cardLevel) {
        this.cardLevel = cardLevel;
    }

    public Rect getCardLevelRect() {
        return cardLevelRect;
    }

    public void setCardLevelRect(Rect cardLevelRect) {
        this.cardLevelRect = cardLevelRect;
    }
}
