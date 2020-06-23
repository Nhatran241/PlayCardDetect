package com.machinelearning.playcarddetect.data.card;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Suit {
    private Bitmap suitImage;
    private Rect suitRect;
    private SuitType suitType;

    public Bitmap getSuitImage() {
        return suitImage;
    }

    public void setSuitImage(Bitmap suitImage) {
        this.suitImage = suitImage;
    }

    public enum SuitType {
        Co,
        Ro,
        Chuon,
        Bich,
        NotDetect
    }

    public Suit(Bitmap suitImage, Rect suitRect, SuitType suitType) {
        this.suitImage = suitImage;
        this.suitRect = suitRect;
        this.suitType = suitType;
    }

    public Rect getSuitRect() {
        return suitRect;
    }

    public void setSuitRect(Rect suitRect) {
        this.suitRect = suitRect;
    }

    public SuitType getSuitType() {
        return suitType;
    }

    public void setSuitType(SuitType suitType) {
        this.suitType = suitType;
    }
}
