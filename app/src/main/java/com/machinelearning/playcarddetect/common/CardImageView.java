package com.machinelearning.playcarddetect.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.Nullable;

public class CardImageView extends ImageView {
    public CardImageView(Context context) {
        super(context);
    }

    public CardImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CardImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CardImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec*2);
    }
}
