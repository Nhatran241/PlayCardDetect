package com.machinelearning.playcarddetect.modules.admin.binding

import android.graphics.Bitmap
import android.graphics.Rect
import android.widget.ImageView
import androidx.databinding.BindingAdapter


@BindingAdapter("setUpImageBitmap")
fun ImageView.setUpImageBitmap(bitmap: Bitmap){
    setImageBitmap(bitmap)
}

@BindingAdapter("setUpCardPosition")
fun ImageView.setUpCardPosition(rect: Rect){

}