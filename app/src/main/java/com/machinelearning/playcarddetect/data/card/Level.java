package com.machinelearning.playcarddetect.data.card;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Level {
    private String cardLevel;
    private int[] pixels;
    private int width;
    private int height;
    private Bitmap bitmap;

    public int[] getPixels() {
        return pixels;
    }

    public void setPixels(int[] pixels) {
        this.pixels = pixels;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Level(String cardLevel, int[] pixels, int width, int height, Bitmap bitmap) {
        this.cardLevel = cardLevel;
        this.pixels = pixels;
        this.width = width;
        this.height = height;
        this.bitmap = bitmap;
    }


    public int[] getPixel() {
        return pixels;
    }

    public void setPixel(int[] pixels) {
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

    public String getCardLevel() {
        return cardLevel;
    }

    public void setCardLevel(String cardLevel) {
        this.cardLevel = cardLevel;
    }

}
