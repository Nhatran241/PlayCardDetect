package com.machinelearning.playcarddetect.common.model;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Card {
  private Bitmap cardBitmap;
  private Rect cardRect;

    public Bitmap getCardBitmap() {
        return cardBitmap;
    }

    public void setCardBitmap(Bitmap cardBitmap) {
        this.cardBitmap = cardBitmap;
    }

    public Rect getCardRect() {
        return cardRect;
    }

    public void setCardRect(Rect cardRect) {
        this.cardRect = cardRect;
    }

    public Card(Bitmap cardBitmap, Rect cardRect) {
        this.cardBitmap = cardBitmap;
        this.cardRect = cardRect;
    }
    public String getBase64(){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        cardBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

}
