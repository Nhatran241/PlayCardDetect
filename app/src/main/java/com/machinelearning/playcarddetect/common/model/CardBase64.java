package com.machinelearning.playcarddetect.common.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class CardBase64 {
    private String cardBitmap64;
    private Rect cardRect;

    public CardBase64(String cardBitmap64, Rect cardRect) {
        this.cardBitmap64 = cardBitmap64;
        this.cardRect = cardRect;
    }
    public CardBase64() {
    }

    public String getCardBitmap64() {
        return cardBitmap64;
    }

    public void setCardBitmap64(String cardBitmap64) {
        this.cardBitmap64 = cardBitmap64;
    }

    public Rect getCardRect() {
        return cardRect;
    }

    public void setCardRect(Rect cardRect) {
        this.cardRect = cardRect;
    }
    public static Bitmap convert(String base64Str) throws IllegalArgumentException
    {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",")  + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static String convert(Bitmap bitmap)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
    }

}
