package com.machinelearning.playcarddetect.process;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.machinelearning.playcarddetect.data.MyAdapter;
import com.machinelearning.playcarddetect.data.card.Card;
import com.machinelearning.playcarddetect.data.card.Level;
import com.machinelearning.playcarddetect.data.card.Suit;

import java.util.ArrayList;
import java.util.List;

public class GetCardDataManager {
    private static final int BITMAPWIDTH = 720;
    private static GetCardDataManager instance;
    private TextRecognizer recognizer;

    public static GetCardDataManager getInstance() {
        if(instance==null)
            instance = new GetCardDataManager();
        return instance;
    }
    public void getCardDataFromBitmap(Bitmap baseBitmap,GetCardDataFromBitmapListener getCardDataFromBitmapListener){
        List<Card> cardList = new ArrayList<>();
        Bitmap resizedBitmap = getResizeBitmap(baseBitmap);
        baseBitmap.recycle();
        if(recognizer ==null)
            recognizer = TextRecognition.getClient();
        recognizer.process(getRecongnizerImage(resizedBitmap))
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {

                        for (int i = 0; i < visionText.getTextBlocks().size(); i++) {
                            for (Text.Line line : visionText.getTextBlocks().get(i).getLines()) {
                                for (Text.Element element : line.getElements()) {
                                    Rect textRect = element.getBoundingBox();
                                    String cardName = element.getText().trim();
                                    if (isCard(resizedBitmap, textRect)) {
                                        if (cardName.length() == 1||cardName.equals("10")) {
                                            Log.d("nhatnhat", "onSuccess: "+cardName);
                                            if (cardName.equals("A") || cardName.equals("2") || cardName.equals("3") ||
                                                    cardName.equals("4") || cardName.equals("5") || cardName.equals("6") ||
                                                    cardName.equals("7") || cardName.equals("8") || cardName.equals("9") ||
                                                    cardName.equals("10") || cardName.equals("J") || cardName.equals("Q") ||
                                                    cardName.equals("K") || cardName.equals("1") || cardName.equals("0")) {
//                                                textRect.top=textRect.top;
                                                textRect.left=textRect.centerX()-5;
                                                textRect.right=textRect.centerX()+5;
//                                                textRect.bottom +=h;
                                                Card card =checkSuitOfCard(textRect, resizedBitmap, cardName);
                                                if(card!=null)
                                                    cardList.add(card);
                                            }
                                        } else {

                                            Log.d("nhatnhat", "onSuccess: "+cardName+"|"+textRect.left+" : "+textRect.right+"ti le" +(resizedBitmap.getWidth()/(textRect.right-textRect.left)));
                                            int length = cardName.length();
                                            if(cardName.contains("10"))
                                                length-=1;
                                            int rectW = textRect.right - textRect.left;
                                            for (int j = 0; j < length; j++) {
                                                Rect rect = new Rect();
                                                rect.left = textRect.left + ((rectW / length) * j);
                                                rect.right = rect.left + rectW/length;
                                                Log.d("nhatnhat", "onSuccess: "+cardName.charAt(j)+" : "+rect.left +"/"+rect.right+"");
//                                                rect.right = rect.right-(rect.right-rect.left)/2;
//                                                int h = (textRect.bottom-textRect.top)*2/3;
                                                rect.top=textRect.top;
                                                rect.bottom =textRect.bottom;
                                                if (cardName.charAt(j)=='A' ||cardName.charAt(j)=='2' ||cardName.charAt(j)=='3' ||
                                                        cardName.charAt(j)=='4' ||cardName.charAt(j)=='5' ||cardName.charAt(j)=='6' ||
                                                        cardName.charAt(j)=='7' ||cardName.charAt(j)=='8' ||cardName.charAt(j)=='9' ||
                                                        cardName.charAt(j)=='J' ||cardName.charAt(j)=='Q' ||cardName.charAt(j)=='K' ) {
                                                    Card card = checkSuitOfCard(rect, resizedBitmap, cardName.charAt(j) + "");
                                                    if (card != null)
                                                        cardList.add(card);
                                                }else if(cardName.charAt(j)=='1'){
                                                    Card card = checkSuitOfCard(rect, resizedBitmap, "10");
                                                    if (card != null)
                                                        cardList.add(card);
                                                }
                                            }
                                        }
                                    }

                                }
                            }

                        }
                        getCardDataFromBitmapListener.onGetDataCompleted(cardList);
                    }

                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                getCardDataFromBitmapListener.onGetDataFailed(e.toString());
                            }
                        });
    }

    private InputImage getRecongnizerImage(Bitmap baseBitmap) {
       return InputImage.fromBitmap(baseBitmap, 0);
    }

    private Bitmap getResizeBitmap(Bitmap baseBitmap) {
        float ratio = baseBitmap.getWidth() / baseBitmap.getHeight();
        return Bitmap.createScaledBitmap(baseBitmap, BITMAPWIDTH, (int) (BITMAPWIDTH / ratio), false);
    }
    private Card checkSuitOfCard(Rect rect, Bitmap bitmap, String cardname) {


        /**
         * It not a Card
         */
        if (rect.right-rect.left<=0||rect.bottom-rect.top<=0) {
            Log.d("nhatnhat", "onsuccess: " + cardname + " not a card");
            return null;
        }

        int h = (rect.bottom-rect.top)*2/3;
        Rect suitRect = new Rect();
        suitRect.left = rect.left;
        suitRect.right = rect.right;
        suitRect.top = rect.bottom+1;
        suitRect.bottom = rect.bottom+h;
        Suit cardSuit = getSuitOnlyBitmap(suitRect,bitmap,cardname);
        Bitmap suitBitmapImage =cardSuit.getSuitImage();
        int[] coverImageIntArray1D = new int[suitBitmapImage.getWidth() * suitBitmapImage.getHeight()];
        suitBitmapImage.getPixels(coverImageIntArray1D, 0, suitBitmapImage.getWidth(),
                0, 0, suitBitmapImage.getWidth(), suitBitmapImage.getHeight());
        int[] firstMatchFirstRow = new int[2];
        int[] lastMatchChangeSideRow = new int[2];
        int[] lastMatchFirstRow = new int[2];
        int[] lastMatchLastRow = new int[2];
        int[] lastMatchMaxRow = new int[2];

//        firstMatchFirstRow[0] = 0;
//        firstMatchFirstRow[1] = 0;
//
//        lastMatchFirstRow[0] = 0;
//        lastMatchFirstRow[1] = 0;
//
//        lastMatchLastRow[0] = 0;
//        lastMatchLastRow[1] = 0;
//
//        lastMatchChangeSideRow[0] = 0;
//        lastMatchChangeSideRow[1] = 0;
//        lastMatchMaxRow[0] = 0;
//        lastMatchMaxRow[1] = 0;

        int previousRowPixelCount=0;
        int maxRowDifferentPixelUp =0;
        int maxRowDifferentPixelDown=0;
        for (int i = 0; i < suitBitmapImage.getHeight(); i++) {
            String pixelinRow = "";
            int maxInRow = 0;
            for (int j = 0; j < suitBitmapImage.getWidth(); j++) {
                int pixel = coverImageIntArray1D[j + i * suitBitmapImage.getWidth()];
                int red = Color.red(pixel);
                String reds = red + "";
                if (red < 100)
                    reds = 0 + "" + red;
                pixelinRow += reds + "   ";
                if (red <= 120) {
                    maxInRow=j;
                    lastMatchLastRow[0] = j;
                    lastMatchLastRow[1] = i;
                    if (firstMatchFirstRow[0] == 0) {
                        firstMatchFirstRow[0] = j;
                        firstMatchFirstRow[1] = i;
                    }
                    if (i == firstMatchFirstRow[1]) {
                        lastMatchFirstRow[0] = j;
                        lastMatchFirstRow[1] = i;
                    }
                }

            }
            if(maxInRow>=previousRowPixelCount) {
                int differentPixelCountInRow = maxInRow - previousRowPixelCount;
                if (differentPixelCountInRow > maxRowDifferentPixelUp && previousRowPixelCount!=0)
                    maxRowDifferentPixelUp = differentPixelCountInRow;

            }else {
                int differentPixelCountInRow = previousRowPixelCount-maxInRow;
                if (differentPixelCountInRow > maxRowDifferentPixelDown && maxInRow!=0)
                    maxRowDifferentPixelDown = differentPixelCountInRow;
            }
            previousRowPixelCount = maxInRow;
            Log.d("nhatnhat", ": "+pixelinRow);



        }

        Rect fullCardRect = new Rect();
        fullCardRect.left = cardSuit.getSuitRect().left;
        fullCardRect.right = cardSuit.getSuitRect().right;
        fullCardRect.top = rect.top;
        fullCardRect.bottom = cardSuit.getSuitRect().bottom;
        Level cardLevel = new Level(cardname,rect);
        cardLevel.setCardLevelBitmap(Bitmap.createBitmap(bitmap,rect.left,rect.top,rect.right-rect.left,rect.bottom-rect.top));
        if(maxRowDifferentPixelUp > 2){
            cardSuit.setSuitType(Suit.SuitType.Chuon);
            Log.d("nhatnhat", ": "+cardLevel.getCardLevel()+" : "+cardSuit.getSuitType().name());
            return new Card(cardLevel,cardSuit,fullCardRect);
        }else {
            if(lastMatchFirstRow[0]-lastMatchLastRow[0]>2){
                cardSuit.setSuitType(Suit.SuitType.Co);
                Log.d("nhatnhat", ": "+cardLevel.getCardLevel()+" : "+cardSuit.getSuitType().name());
                return new Card(cardLevel,cardSuit,fullCardRect);
            }else if(maxRowDifferentPixelDown >2){
                cardSuit.setSuitType(Suit.SuitType.Bich);
                Log.d("nhatnhat", ": "+cardLevel.getCardLevel()+" : "+cardSuit.getSuitType().name());
                return new Card(cardLevel,cardSuit,fullCardRect);
            }else {
                cardSuit.setSuitType(Suit.SuitType.Ro);
                Log.d("nhatnhat", ": "+cardLevel.getCardLevel()+" : "+cardSuit.getSuitType().name());
                return new Card(cardLevel,cardSuit,fullCardRect);
            }
        }
//        int[] vectorAB = new int[2];
//        int[] vectorAC = new int[2];
//        vectorAB[0] = lastMatchFirstRow[0] - firstMatchFirstRow[0];
//        vectorAB[1] = lastMatchFirstRow[1] - firstMatchFirstRow[1];
//
//        vectorAC[0] = lastMatchLastRow[0] - lastMatchFirstRow[0];
//        vectorAC[1] = lastMatchLastRow[1] - lastMatchFirstRow[1];
//
//        double cos = (vectorAB[0] * vectorAC[0] + vectorAB[1] * vectorAC[1])
//                / (Math.sqrt((vectorAB[0] * vectorAB[0]) + (vectorAB[1] * vectorAB[1])) * Math.sqrt((vectorAC[0] * vectorAC[0]) + (vectorAC[1] * vectorAC[1])));
////        Log.d(TAG, "checkSuitOfCard: "+cardname +firstMatchFirstRow[0] +"|"+firstMatchFirstRow[1]);
////        Log.d(TAG, "checkSuitOfCard: "+cardname +lastMatchFirstRow[0] +"|"+lastMatchFirstRow[1]);
////        Log.d(TAG, "checkSuitOfCard: "+cardname +lastMatchLastRow[0]+"|"+lastMatchLastRow[1] );
//        Log.d(TAG, "checkSuitOfCard: " + cardname + " góc :" + Math.toDegrees(Math.cos(cos)));
    }

    private Suit getSuitOnlyBitmap(Rect rect, Bitmap bitmap, String cardname) {
        Rect remainRect =new Rect();
        remainRect.left = rect.left;
        remainRect.right = rect.right;
        remainRect.top = rect.top;
        remainRect.bottom = rect.bottom;
        Bitmap smallBitmap = bitmap.createBitmap(bitmap, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top);
        int[] coverImageIntArray1D = new int[smallBitmap.getWidth() * smallBitmap.getHeight()];
        smallBitmap.getPixels(coverImageIntArray1D, 0, smallBitmap.getWidth(),
                0, 0, smallBitmap.getWidth(), smallBitmap.getHeight());
        boolean lostLeft=false;
        boolean lostTop=false;
        boolean lostBot =false;
        boolean lostRight =false;
        for (int i = 0; i < smallBitmap.getHeight(); i++) {
            int pixelL = coverImageIntArray1D[0 + i * smallBitmap.getWidth()];
            int pixelR = coverImageIntArray1D[smallBitmap.getWidth()-1 + i * smallBitmap.getWidth()];
            if(Color.red(pixelL)<100){
                lostLeft=true;
            }
            if(Color.red(pixelR)<100){
                lostRight=true;
            }

        }
        for (int i = 0; i < smallBitmap.getWidth(); i++) {
            int pixelT = coverImageIntArray1D[i];
            int pixelB = coverImageIntArray1D[(smallBitmap.getHeight()*smallBitmap.getWidth())-1-i];
            if(Color.red(pixelT)<100){
                lostTop=true;
            }
            if(Color.red(pixelB)<100){
                lostBot=true;
            }
        }


        if(lostLeft)
            rect.left-=2;
        if(lostRight)
            rect.right+=2;
        if(lostBot)
            rect.bottom+=2;
        if(lostTop)
            rect.top-=2;
        if(rect.left!=remainRect.left||rect.right!=remainRect.right||rect.top!=remainRect.top||rect.bottom!=remainRect.bottom){
            return getSuitOnlyBitmap(rect,bitmap,cardname);
        }else {
            return new Suit(smallBitmap,rect, Suit.SuitType.NotDetect);
        }
    }

    private boolean isCard(Bitmap bitmap1, Rect textRect) {
        int[] coverImageIntArray1D = new int[bitmap1.getWidth() * bitmap1.getHeight()];
        bitmap1.getPixels(coverImageIntArray1D, 0, bitmap1.getWidth(),
                0, 0, bitmap1.getWidth(), bitmap1.getHeight());

        int pixelleft = coverImageIntArray1D[textRect.left + textRect.centerY() * bitmap1.getWidth()];
        int pixelright = coverImageIntArray1D[textRect.right + textRect.centerY() * bitmap1.getWidth()];
        int pixeltop = coverImageIntArray1D[textRect.centerX() + textRect.top * bitmap1.getWidth()];
        int pixelbot = coverImageIntArray1D[textRect.centerX() + textRect.bottom * bitmap1.getWidth()];
        int redl = Color.red(pixelleft);
        int redr = Color.red(pixelright);
        int redt = Color.red(pixeltop);
        int redb = Color.red(pixelbot);
        if (redl > 210 || redr > 210 || redt > 210 || redb > 210)
            return true;
        return false;
    }
    public interface GetCardDataFromBitmapListener{
        void onGetDataCompleted(List<Card> cards);
        void onGetDataFailed(String error);
    }

}
