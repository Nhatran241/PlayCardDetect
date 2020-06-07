package com.machinelearning.playcarddetect.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import com.machinelearning.playcarddetect.SaveImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CardsManager {
    private static CardsManager instance;
    private OnCardSplistListener onCardSplistListener;

    public static CardsManager getInstance() {
        if(instance==null)
            instance=new CardsManager();
        return instance;
    }
    public void process(Bitmap bitmap, Context context,OnCardSplistListener onCardSplistListener){
//        List<Bitmap> cardsBitmap = splistCardsFromBitmap(13,407,538,59,55,bitmap);
        List<Bitmap> cardsBitmap = splistCardsFromBitmap(13,407,590,58,50,bitmap);
//        List<Bitmap> cardsBitmap = splistCardsFromBitmap(13,407,538,59,60,bitmap);
//        List<Bitmap> suits = splistCardsFromBitmap(1,407,538+60,59,60,bitmap);

        onCardSplistListener.OnCardSplistCompleted(cardsBitmap);

    }
    public List<Bitmap> splistCardsFromBitmap(int numcard,int startX,int startY,int cardW,int cardH,Bitmap bitmap){
        List<Bitmap> cardsBitmap = new ArrayList<>();
        for (int i = 0; i <numcard ; i++) {
            Bitmap bitmap1 = Bitmap.createBitmap(bitmap,startX+cardW*i,startY,cardW,cardH);
            cardsBitmap.add(bitmap1);
        }


        return cardsBitmap;
    }
    public interface OnCardSplistListener{
       void OnCardSplistCompleted(List<Bitmap> cards);
    }
}
