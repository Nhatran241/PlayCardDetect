package com.machinelearning.playcarddetect.data.card;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Suit {
    private SuitType suitType;
    private int[] pixels;
    private int width;
    private int height;
    private Bitmap bitmap ;


    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Suit( SuitType suitType, int[] pixels, int width, int height, Bitmap bitmap) {
        this.suitType = suitType;
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.bitmap = bitmap;
    }

    public enum SuitType {
        Co,
        Ro,
        Chuon,
        Bich,
        NotDetect
    }

    public int[] getPixels() {
        return pixels;
    }

    public void setPixels(int[] pixels) {
        this.pixels = pixels;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public SuitType getSuitType() {
        return suitType;
    }

    public void setSuitType(SuitType suitType) {
        this.suitType = suitType;
    }
}
