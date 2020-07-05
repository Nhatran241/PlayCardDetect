package com.machinelearning.playcarddetect.process;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
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


    private String checkNumber(int[] pixels, int patternColor, boolean lessThan, int width, int height) {
        List<int[]> listMatch = new ArrayList<>();
        List<int[]> listMatchInRow = new ArrayList<>();
        boolean isK =false;
        boolean isA=false;
        boolean is10=true;
        int maxMatchInRowCount =0;
        for (int i = 0; i <height; i++) {
            String row = "";
            int[] matchInRow = new int[2];
            matchInRow[0]=-1;
            matchInRow[1]=-1;
            for (int j = 0; j < width; j++) {
                int pixel = pixels[j + i * width];
//                String red = Color.red(pixel)+"";
//                if(red.trim().length()==1){
//                    red="0"+red;
//                }
//                if(red.trim().length()==2){
//                    red="0"+red;
//                }
//                row+=red+" :";
                if(lessThan) {
                    if (Color.red(pixel) <patternColor){
                        if(matchInRow[0]==-1){
                            matchInRow[0]=j;
                            matchInRow[1]=j;
                        }else {
                            matchInRow[1]=j;
                        }
                        if(j==width-1){
                            listMatchInRow.add(matchInRow);
                            listMatch.add(matchInRow);
                        }
                    }else {
                        if(matchInRow[0]!=-1){
                            listMatch.add(matchInRow);
                            listMatchInRow.add(matchInRow);
                            matchInRow = new int[2];
                            matchInRow[0]=-1;
                            matchInRow[1]=-1;
                        }
                    }
                }else {
                    if (Color.red(pixel) >=patternColor){

                    }
                }
            }
//            Log.d("nhatnhat", "checkNumber: "+row);
            int matchInRowCount=0;
            if(listMatchInRow.size()>1){
                if(listMatchInRow.size()==2&&listMatchInRow.size()==listMatch.size()){
                    isK=true;
                }
                if(i==height-1){
                    if(!isK)
                        isA=true;
                }
                is10=false;
                matchInRowCount=listMatchInRow.get(listMatchInRow.size()-1)[1]-listMatchInRow.get(0)[0];
            }else if(listMatchInRow.size()==1) {
                matchInRowCount = listMatchInRow.get(0)[1]-listMatchInRow.get(0)[0];
            }
            if(matchInRowCount>maxMatchInRowCount){
                maxMatchInRowCount=matchInRowCount;
            }

            listMatchInRow.clear();
        }

        List<Rect> rects = getRectsMathPattern(pixels,width,height,patternColor,false);
        List<Rect> rectInside = new ArrayList<>();
        List<Rect> rectOutside = new ArrayList<>();
        Rect maxRectInside = new Rect();
        Rect maxRectOutside = new Rect();
        Rect premaxRectOutside = new Rect();
        for (int i = 0; i <rects.size() ; i++) {
            if(rects.get(i).left>0&&rects.get(i).right<width-1&&rects.get(i).top>0&&rects.get(i).bottom<height-1){
                rectInside.add(rects.get(i));
                // Inside number
                if(((rects.get(i).bottom-rects.get(i).top)*(rects.get(i).right-rects.get(i).left))>(maxRectInside.bottom-maxRectInside.top)*(maxRectInside.right-maxRectInside.left)){
                    maxRectInside = rects.get(i);
                }
            }else {
                Rect rect =rects.get(i);
                if(rect.left>=0&&rect.right>=0&&rect.top>=0&&rect.bottom>=0){
                    rectOutside.add(rects.get(i));
                    if(((rects.get(i).bottom-rects.get(i).top)*(rects.get(i).right-rects.get(i).left))>(maxRectOutside.bottom-maxRectOutside.top)*(maxRectOutside.right-maxRectOutside.left)){
                        maxRectOutside = rects.get(i);
                    }


                }
            }

        }
        for (int i = 0; i < rectOutside.size(); i++) {
            if(((rectOutside.get(i).bottom-rectOutside.get(i).top)*(rectOutside.get(i).right-rectOutside.get(i).left))<((maxRectOutside.bottom-maxRectOutside.top)*(maxRectOutside.right-maxRectOutside.left))&&((rectOutside.get(i).bottom-rectOutside.get(i).top)*(rectOutside.get(i).right-rectOutside.get(i).left))>(premaxRectOutside.bottom-premaxRectOutside.top)*(premaxRectOutside.right-premaxRectOutside.left)){
                premaxRectOutside = rectOutside.get(i);
            }
        }
        if(rectInside.size()==2){
            //Q,8,
            int sqrFirst =((rectInside.get(0).right-rectInside.get(0).left)*(rectInside.get(0).bottom-rectInside.get(0).top));
            int sqrSecond =((rectInside.get(1).right-rectInside.get(1).left)*(rectInside.get(1).bottom-rectInside.get(1).top));
            if(sqrFirst>sqrSecond){
                return "Q";
            }else return "8";

        }else if(rectInside.size() ==1) {
            if(isA)
                return "A";
            if (maxRectInside.top < height / 4) {
                return "9";
            } else {
                    int sqrFirst =((rectOutside.get(0).right-rectOutside.get(0).left)*(rectOutside.get(0).bottom-rectOutside.get(0).top));
                    int sqrMaxInside =((maxRectInside.right-maxRectInside.left)*(maxRectInside.bottom-maxRectInside.top));

                    if(sqrFirst<sqrMaxInside){
                        return "6";
                    }else return "4";
            }
        }else {
            //2,3,5,7,10,J,K
            if(isK)
                return "K";
            if(is10)
                return "10";
            //2,3,5,7,J
            List<int[]> listMatchInFirstColum = new ArrayList<>();
            List<int[]> listMatchInLastColum = new ArrayList<>();
            List<int[]> listMatchInMinLeftColum = new ArrayList<>();
            List<int[]> listMatchInMaxColum = new ArrayList<>();
            int[] matchInFirstColum = new int[2];
            matchInFirstColum[0]=-1;
            matchInFirstColum[1]=-1;
            int[] matchInLastColum = new int[2];
            matchInLastColum[0]=-1;
            matchInLastColum[1]=-1;
            int[] matchInMinLeftColum = new int[2];
            matchInMinLeftColum[0]=-1;
            matchInMinLeftColum[1]=-1;
            int[] matchInMaxColum = new int[2];
            matchInMaxColum[0]=-1;
            matchInMaxColum[1]=-1;

            int minLeft=1000;
            int max =-1;
            for (int i = 0; i <listMatch.size() ; i++) {
                if(listMatch.get(i)[0]< minLeft)
                    minLeft =listMatch.get(i)[0];
                if(listMatch.get(i)[1]> max)
                    max =listMatch.get(i)[1];
            }

            for (int i = 0; i <height; i++) {
                int pixelFirstColum = pixels[listMatch.get(0)[0] + i * width];
                if (Color.red(pixelFirstColum) < patternColor) {
                    if (matchInFirstColum[0] == -1) {
                        matchInFirstColum[0] = i;
                        matchInFirstColum[1] = i;
                    } else {
                        matchInFirstColum[1] = i;
                    }
                    if (i == height - 1) {
                        listMatchInFirstColum.add(matchInFirstColum);
                    }
                } else {
                    if (matchInFirstColum[0] != -1) {
                        listMatchInFirstColum.add(matchInFirstColum);
                        matchInFirstColum = new int[2];
                        matchInFirstColum[0] = -1;
                        matchInFirstColum[1] = -1;
                    }
                }
                int pixelLastColum = pixels[listMatch.get(0)[1] + i * width];
                if (Color.red(pixelLastColum) < patternColor) {
                    if (matchInLastColum[0] == -1) {
                        matchInLastColum[0] = i;
                        matchInLastColum[1] = i;
                    } else {
                        matchInLastColum[1] = i;
                    }
                    if (i == height - 1) {
                        listMatchInLastColum.add(matchInLastColum);
                    }
                } else {
                    if (matchInLastColum[0] != -1) {
                        listMatchInLastColum.add(matchInLastColum);
                        matchInLastColum = new int[2];
                        matchInLastColum[0] = -1;
                        matchInLastColum[1] = -1;
                    }
                }
                int pixelMinLeftColum = pixels[minLeft + i * width];
                if (Color.red(pixelMinLeftColum) < patternColor) {
                    if (matchInMinLeftColum[0] == -1) {
                        matchInMinLeftColum[0] = i;
                        matchInMinLeftColum[1] = i;
                    } else {
                        matchInMinLeftColum[1] = i;
                    }
                    if (i == height - 1) {
                        listMatchInMinLeftColum.add(matchInMinLeftColum);
                    }
                } else {
                    if (matchInMinLeftColum[0] != -1) {
                        listMatchInMinLeftColum.add(matchInMinLeftColum);
                        matchInMinLeftColum = new int[2];
                        matchInMinLeftColum[0] = -1;
                        matchInMinLeftColum[1] = -1;
                    }
                }
                int pixelMaxColum = pixels[max + i * width];
                if (Color.red(pixelMaxColum) < patternColor) {
                    if (matchInMaxColum[0] == -1) {
                        matchInMaxColum[0] = i;
                        matchInMaxColum[1] = i;
                    } else {
                        matchInMaxColum[1] = i;
                    }
                    if (i == height - 1) {
                        listMatchInMaxColum.add(matchInMaxColum);
                    }
                } else {
                    if (matchInMaxColum[0] != -1) {
                        listMatchInMaxColum.add(matchInMaxColum);
                        matchInMaxColum = new int[2];
                        matchInMaxColum[0] = -1;
                        matchInMaxColum[1] = -1;
                    }
                }
            }

            //2,3,5,7,J
            if(listMatchInFirstColum.size()==1){
                if(listMatch.get(0)[1]-listMatch.get(0)[0]>=width*8/10&&
                        listMatch.get(listMatch.size()-1)[1]-listMatch.get(listMatch.size()-1)[0]<=width/2&&
                rectOutside.size()>=2){
                        return "7";
                }
                return "Not Detected";
            }else if(listMatchInFirstColum.size()==2){
                //3,5,J
                if(listMatchInLastColum.size()==1){
                    if(rectOutside.size()==3) {
                        return "J";
                    }else {
                        return "Not Detected";
                    }
                }else {
                    if(premaxRectOutside.left>width/2){
                        //3
                        return "3";
                    }else {
                        //5
                        return "5";
                    }
                }
            }else {
                if(listMatch.get(listMatch.size()-1)[1]-listMatch.get(listMatch.size()-1)[0]>width*8/10){
                    return "2";
                }else return "Not Detected";
            }

        }

    }

    public Suit.SuitType checkSuit(int[] pixels,int patternColor,boolean lessThan,int width,int height){
        List<int[]> listMatch = new ArrayList<>();
        List<int[]> listMatchInRow = new ArrayList<>();
        boolean isCo =false;
        boolean isChuon =false;
        boolean notMax =true;
        int maxMatchInRowCount =0;
        int preValue =0;
        int maxValueInThisRow=0;
        for (int i = 0; i <height; i++) {
            int[] matchInRow = new int[2];
            matchInRow[0]=-1;
            matchInRow[1]=-1;

            String row ="";
            for (int j = 0; j < width; j++) {
                int pixel = pixels[j + i * width];
//                String red = Color.red(pixel)+"";

                if(lessThan) {
                    if (Color.red(pixel) <patternColor){
                        row+=" 0";
                        if(j==width-1)
                            notMax=false;
                        maxValueInThisRow=j;
                        if(matchInRow[0]==-1){
                            matchInRow[0]=j;
                            matchInRow[1]=j;
                        }else {
                            matchInRow[1]=j;
                        }
                        if(j==width-1){
                            listMatchInRow.add(matchInRow);
                            listMatch.add(matchInRow);
                        }
                    }else {

                        row+=" 1";
                        if(matchInRow[0]!=-1){
                            listMatch.add(matchInRow);
                            listMatchInRow.add(matchInRow);
                            matchInRow = new int[2];
                            matchInRow[0]=-1;
                            matchInRow[1]=-1;
                        }
                    }
                }
            }
            if(preValue!=0){
                if(notMax&&maxValueInThisRow<preValue){
                    Log.d("checksuit", "checkSuit: "+maxValueInThisRow+"/"+preValue);
                    isChuon=true;
                }
            }else {
                preValue = maxValueInThisRow;
            }
            Log.d("checksuit", "checkSuit: "+row);
            int matchInRowCount=0;
            if(listMatchInRow.size()>1){
                if(listMatchInRow.size()==listMatch.size())
                    isCo=true;
                matchInRowCount=listMatchInRow.get(listMatchInRow.size()-1)[1]-listMatchInRow.get(0)[0];
            }else if(listMatchInRow.size()==1) {
                matchInRowCount = listMatchInRow.get(0)[1]-listMatchInRow.get(0)[0];
            }
            if(matchInRowCount>maxMatchInRowCount){
                maxMatchInRowCount=matchInRowCount;
            }

            listMatchInRow.clear();
        }



        List<Rect> rects = getRectsMathPattern(pixels,width,height,patternColor,false);
        List<Rect> rectInside = new ArrayList<>();
        List<Rect> rectOutside = new ArrayList<>();
        Rect maxRectInside = new Rect();
        Rect maxRectOutside = new Rect();
        for (int i = 0; i <rects.size() ; i++) {
            if(rects.get(i).left>0&&rects.get(i).right<width-1&&rects.get(i).top>0&&rects.get(i).bottom<height-1){
                rectInside.add(rects.get(i));
                // Inside number
                if(((rects.get(i).bottom-rects.get(i).top)*(rects.get(i).right-rects.get(i).left))>(maxRectInside.bottom-maxRectInside.top)*(maxRectInside.right-maxRectInside.left)){
                    maxRectInside = rects.get(i);
                }
            }else {
                Rect rect =rects.get(i);
                if(rect.left>=0&&rect.right>=0&&rect.top>=0&&rect.bottom>=0){
                    rectOutside.add(rects.get(i));
                    if(((rects.get(i).bottom-rects.get(i).top)*(rects.get(i).right-rects.get(i).left))>(maxRectOutside.bottom-maxRectOutside.top)*(maxRectOutside.right-maxRectOutside.left)){
                        maxRectOutside = rects.get(i);
                    }


                }
            }

        }


        if(rectInside.size()>0){
            for (int i = 0; i <rectInside.size() ; i++) {
                Rect rect = rectInside.get(i);
                if((rect.right-rect.left)*(rect.bottom-rect.top)>1){
                    return Suit.SuitType.NotDetect;
                }
            }
        }
        if(isCo){
            return Suit.SuitType.Co;
        }
        int preTbc=0;
        int maxDif=0;
        for (int i = 0; i <width; i++) {
            int tbc=0;
            for (int j = 0; j < height/2; j++) {
                int pixel = pixels[i + j * width];
                if(lessThan) {
                    if (Color.red(pixel) <patternColor){
                        tbc+=1+(height/2);
                    }
                }
            }

            if(preTbc!=0){
                if(tbc-preTbc>maxDif){
                    maxDif=tbc-preTbc;
                }
            }
                preTbc =tbc;
        }
        Log.d("nhatnhat", "checkSuit: "+maxDif+"/"+height);
        if(rectOutside.size()==4){
                if(maxDif>(height*2)){
                    return Suit.SuitType.Chuon;
                }else {
                    boolean isBich=false;
                    int preCount=0;
                    for (int i = height-1; i >height/2 ; i--) {
                        int cout=0;
                        for (int j = 0; j <width ; j++) {
                            int pixel = pixels[j + i * width];
                                if (Color.red(pixel) <patternColor){
                                    cout++;
                                }
                        }
                        if(preCount!=0){
                            if(cout<preCount){
                                isBich=true;
                            }
                        }
                        preCount =cout;
                    }
                    if(isBich){
                        return Suit.SuitType.Bich;
                    }else {
                        return Suit.SuitType.Ro;
                    }
                }
        }
        return Suit.SuitType.NotDetect;
    }
    public List<Card> getCardsZoneBitmap(Bitmap baseBitmap , Rect zoneContainCardsZone,int patternColum,int patternZone){
        List<Card> cardsInZone = new ArrayList<>();
        List<Rect> getCards = getRectsMathPattern(baseBitmap,patternZone,false);

        String number ="";
        Suit.SuitType suitType = Suit.SuitType.NotDetect;
        for (Rect rect:
                getCards) {

            // Lá bài có độ rộng lớn hơn 10 và độ cao lớn 10
            if(rect.right-rect.left>=10 && rect.bottom-rect.top>10) {
                number = "";
                suitType = Suit.SuitType.NotDetect;
                Bitmap bitmap = Bitmap.createBitmap(baseBitmap, rect.left, rect.top, (rect.right - rect.left)+1, (rect.bottom - rect.top)+1);

                List<Rect> numberAndSuit = getRectsMathPattern(bitmap,150,true);
                if(numberAndSuit.size()>0) {
                    outsideloop:
                    for (int i = 0; i <numberAndSuit.size() ; i++) {
                        Rect rect1 = numberAndSuit.get(i);
                            Bitmap bitmap2 = Bitmap.createBitmap(bitmap, rect1.left, rect1.top, (rect1.right - rect1.left)+1, (rect1.bottom - rect1.top)+1);
                            int[] pixels = new int[bitmap2.getWidth() * bitmap2.getHeight()];
                            bitmap2.getPixels(pixels, 0, bitmap2.getWidth(),
                                    0, 0, bitmap2.getWidth(), bitmap2.getHeight());
                            if(i==0){
                                number =checkNumber(pixels,150,true,bitmap2.getWidth(),bitmap2.getHeight());
//                                Log.d("nhatnhat", ": "+number);
//                                if(!number.equals("Not Detected")){
//                                    Card card2 = new Card();
//                                    Level level2 = new Level("null", null, bitmap2.getWidth(), bitmap2.getHeight(), bitmap2);
//                                    card2.setCardLevel(level2);
//                                    cardsInZone.add(card2);
//                                }
                            }
                            else {
                                if(!number.equals("Not Detected")&&!number.equals("")){
//                                        if(bitmap2.getWidth()<20||bitmap2.getHeight()<20){
//                                            bitmap2 = Bitmap.createScaledBitmap(bitmap2,20,40,false);
//                                            pixels = new int[bitmap2.getWidth() * bitmap2.getHeight()];
//                                            bitmap2.getPixels(pixels, 0, bitmap2.getWidth(),
//                                                    0, 0, bitmap2.getWidth(), bitmap2.getHeight());
////                                        }
                                       suitType =checkSuit(pixels,150,true,bitmap2.getWidth(),bitmap2.getHeight());
//                                    Card card2 = new Card();
//                                    Level level2 = new Level("null", null, bitmap.getWidth(), bitmap.getHeight(), bitmap);
//                                    card2.setCardLevel(level2);
//                                    cardsInZone.add(card2);
                                    Log.d("checksuit", ""+number+"/"+suitType);
                                    if(suitType!= Suit.SuitType.NotDetect){
                                        Card card2 = new Card();
                                        Level level2 = new Level(number, null, bitmap.getWidth(), bitmap.getHeight(), bitmap);
                                        Suit suit = new Suit(suitType,null,0,0,null);
                                        card2.setCardLevel(level2);
                                        card2.setCardRect(rect1);
                                        card2.setCardsuit(suit);
                                        cardsInZone.add(card2);
                                        break outsideloop;
                                    }
                                }
                            }
                    }

                }
//                if(isCard){
//                    Card card = new Card();
//                    Level level = new Level("null", null, bitmap.getWidth(), bitmap.getHeight(), bitmap);
//                    card.setCardLevel(level);
//                    cardsInZone.add(card);
//                }

            }
        }
        return cardsInZone;


    }

    private List<Card> splistCards(Bitmap cards,int patternColum) {
            List<Card> cardsAfterSplist = new ArrayList<>();
            List<Rect> colums = splistColums(cards,patternColum);
            cardsAfterSplist.addAll(splistCardsByColum(colums,cards));
        return cardsAfterSplist;
    }

    private List<Rect> splistColums(Bitmap cards,int columPattern) {
        List<Rect> colums = new ArrayList<>();
        int[] coverImageIntArray1D = new int[cards.getWidth() * cards.getHeight()];
        cards.getPixels(coverImageIntArray1D, 0, cards.getWidth(),
                0, 0, cards.getWidth(), cards.getHeight());
        Rect colum = new Rect();
        colum.left =0;
        colum.right=0;

        int lowerColor = 255;
        int maxColor =0;
        for (int i = 0; i < cards.getWidth(); i++) {
            int pixel = coverImageIntArray1D[i + 0 * cards.getWidth()];
            if(Color.red(pixel)<lowerColor) {
                lowerColor = Color.red(pixel);
            }else if(Color.red(pixel)>maxColor){
                maxColor = Color.red(pixel);
            }
        }
        int maxDif = maxColor-lowerColor;


        Log.d("nhatnhat", "splistColums: "+maxDif+"/"+maxColor+"/"+lowerColor);
////        outterloop:
        for (int i = 0; i < cards.getHeight(); i++) {
            String row = "";
            for (int j = 0; j < cards.getWidth(); j++) {
                int pixel = coverImageIntArray1D[j + i * cards.getWidth()];
                String red = Color.red(pixel) + " ";
                if (red.trim().length() == 1) {
                    red = "0" + red;
                }
                if (red.trim().length() == 2) {
                    red = "0" + red;
                }
                row += red + " ";
                if (isColum(coverImageIntArray1D, j, 0, cards, maxColor-(maxDif/3))) {
                    if (colum.left == 0) {
                        colum.top = 0;
                        colum.left = j;
                        colum.bottom = 0;
                        colum.right = j;
                    }
                } else {
                    if (colum.right != 0) {
                        colum.right = j - 1;
                        colums.add(colum);
                        colum = new Rect();
                        colum.left = 0;
                        colum.right = 0;
                    }
                }

            }
            Log.d("nhatnhat", "splistColums: "+row);
        }
                Rect startR = new Rect();
                startR.left=0;
                startR.right=1;
                startR.top=0;
                startR.bottom=0;
                colums.add(0,startR);

//            if(colums.size()!=0)
//                break outterloop;
//        }

        return colums;
    }

    private List<Card> splistCardsByColum(List<Rect> colums, Bitmap cards) {
        List<Card> cardsAfterSplist = new ArrayList<>();
        int width=0;
        if(colums.size()>1){
            width = colums.get(1).left;
        }else {
            width = cards.getWidth()/3;
        }

            for (int i = 0; i < colums.size(); i++) {
                Log.d("nhatnhat", "splistCardsByColum: "+colums.size()+"/"+colums.get(i).right+"/"+width+"/"+cards.getWidth());
                Card card = new Card();
                if(width>0&&colums.get(i).right+width<=cards.getWidth()){
                    Bitmap cardBitmap = Bitmap.createBitmap(cards, colums.get(i).right, 0, width, cards.getHeight());
                    /**
                     * Cắt từng number ra khỏi từng card
                     */

                    Card card2 = new Card();
                    Level level2 = new Level("Not Detected", null, cardBitmap.getWidth(), cardBitmap.getHeight(), cardBitmap);

                    card2.setCardLevel(level2);
                    cardsAfterSplist.add(card2);
                    Bitmap levelBitmap = splistSmallestMatchImage(cardBitmap);
                    if(levelBitmap!=null) {
                        int[] pixelsLevel = new int[levelBitmap.getWidth() * levelBitmap.getHeight()];
                        levelBitmap.getPixels(pixelsLevel, 0, levelBitmap.getWidth(),
                                0, 0, levelBitmap.getWidth(), levelBitmap.getHeight());

                        Level level = new Level("Not Detected", pixelsLevel, levelBitmap.getWidth(), levelBitmap.getHeight(), levelBitmap);

                        card.setCardLevel(level);
                    }
                    /**
                     * Đã cắt thành công các number ra khỏi card . ta sẽ dựa vào độ cao của number để cắt nhỏ card ban đầu lại
                     * và tiếp tục cắt các suit
                     */
//                    Bitmap temp = Bitmap.createBitmap(cards, colums.get(0).right, 0, width, cards.getHeight());
//                    Rect numberRect =splistSmallestRect(temp);
//                    int startY =numberRect.bottom+3;
//                    int height =numberRect.bottom-numberRect.top;
//
//                    Bitmap suitBitmap = splistSmallestMatchImage(Bitmap.createBitmap(cards, colums.get(i).right, startY, width - 2, height));
//                    if(suitBitmap!=null) {
//                        int[] pixelsSuit = new int[suitBitmap.getWidth() * suitBitmap.getHeight()];
//                        suitBitmap.getPixels(pixelsSuit, 0, suitBitmap.getWidth(),
//                                0, 0, suitBitmap.getWidth(), suitBitmap.getHeight());
//                        Suit suit = new Suit(Suit.SuitType.NotDetect, pixelsSuit, suitBitmap.getWidth(), suitBitmap.getHeight(), suitBitmap);
//
//                        Rect cardRect = new Rect();
//                        cardRect.left = i * width;
//                        cardRect.right = cardRect.left + width;
//                        cardRect.top = 0;
//                        cardRect.bottom = cardBitmap.getHeight();
//
//                        card.setCardRect(cardRect);
//                        card.setCardsuit(suit);
//                    }
//
//                    cardsAfterSplist.add(card);




                }

            }
        return cardsAfterSplist;
    }

    private Bitmap splistSmallestMatchImage(Bitmap bitmap) {
        Rect smallestRect =splistSmallestRect(bitmap);
           if((smallestRect.right-smallestRect.left)>0&&(smallestRect.bottom-smallestRect.top)>0) {
            Bitmap levelBitmap = Bitmap.createBitmap(bitmap, smallestRect.left, smallestRect.top, smallestRect.right - smallestRect.left, smallestRect.bottom - smallestRect.top);
            return  levelBitmap;
        }
        return null;
    }
    private Rect splistSmallestRect(Bitmap bitmap){
        Rect numberRect = new Rect();
        int[] coverImageIntArray1D2 = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(coverImageIntArray1D2, 0, bitmap.getWidth(),
                0, 0, bitmap.getWidth(), bitmap.getHeight());
        outterloop:
        for (int k = 0; k < bitmap.getHeight(); k++) {
            for (int j = 0; j < bitmap.getWidth(); j++) {
                int pixel = coverImageIntArray1D2[j + k * bitmap.getWidth()];
                if (Color.red(pixel) < 100) { // Tìm điểm đầu tiên của chữ
                    numberRect.top = k;
                    numberRect.left = j;
                    numberRect.bottom =k;
                    numberRect.right =j;
                    break outterloop;
                }
            }
        }
//        followPath(numberRect,, coverImageIntArray1D2, bitmap.getWidth(), bitmap.getHeight(), numberRect.left, numberRect.top);
        return numberRect;
    }

    private boolean isColum(int[] coverImageIntArray1D, int j, int i, Bitmap cards, int patternColum) {
        int pixel = coverImageIntArray1D[j + i * cards.getWidth()];
        if(Color.red(pixel)<patternColum){
            for (int k = 0; k <cards.getHeight() ; k++) {
                if(Color.red(coverImageIntArray1D[j + k * cards.getWidth()])>patternColum){
                    return false;
                }
            }
            return true;
        }else {
            return false;
        }
    }


    private void followPath(Rect numberRect,int PatternZone,boolean lessThanPattern, int[] coverImageIntArray1D, int width,int height, int j, int i) {
        if(j<0||i<0||j>width-1||i>height-1||coverImageIntArray1D[j+i*width]==-1)
            return; // Đã duyệt qua
        if(lessThanPattern) {
            if (Color.red(coverImageIntArray1D[j + i * width]) < PatternZone) {
                if (numberRect.left == -1) {
                    numberRect.left = j;
                    numberRect.top = i;
                }
                if (j < numberRect.left)
                    numberRect.left = j;
                if (j > numberRect.right)
                    numberRect.right = j;
                if (i < numberRect.top)
                    numberRect.top = i;
                if (i > numberRect.bottom)
                    numberRect.bottom = i;
                coverImageIntArray1D[j + i * width] = -1;
                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j - 1, i);
                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j, i - 1);
                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j, i + 1);
                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j + 1, i);
            }
        }else {

            if (Color.red(coverImageIntArray1D[j + i * width]) >= PatternZone) {
                if (numberRect.left == -1) {
                    numberRect.left = j;
                    numberRect.top = i;
                }
                if (j < numberRect.left)
                    numberRect.left = j;
                if (j > numberRect.right)
                    numberRect.right = j;
                if (i < numberRect.top)
                    numberRect.top = i;
                if (i > numberRect.bottom)
                    numberRect.bottom = i;
                coverImageIntArray1D[j + i * width] = -1;
                //                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j - 1, i - 1);
                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j - 1, i);
//                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j - 1, i + 1);
                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j, i - 1);
                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j, i + 1);
//                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j + 1, i - 1);
                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j + 1, i);
//                followPath(numberRect, PatternZone,lessThanPattern, coverImageIntArray1D, width, height, j + 1, i + 1);
            }
        }

    }

    public List<Rect> getRectsMathPattern(Bitmap bitmap,int patternRedColor,boolean lessThanPattern){
        List<Rect> allRectCanBeCardZone = new ArrayList<>();
        int[] coverImageIntArray1D = new int[bitmap.getWidth() * bitmap.getHeight()];
        bitmap.getPixels(coverImageIntArray1D, 0, bitmap.getWidth(),
                0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect cardsZone = new Rect();
        cardsZone.left =-1;
        for (int i = 0; i < bitmap.getHeight(); i++) {
            for (int j = 0; j < bitmap.getWidth(); j++) {
                int pixel = coverImageIntArray1D[j + i * bitmap.getWidth()];
                if(lessThanPattern) {
                    if (Color.red(pixel) <= patternRedColor) {
                        followPath(cardsZone, patternRedColor, true, coverImageIntArray1D, bitmap.getWidth(), bitmap.getHeight(), j, i);
//                        cardsZone.right+=1;
                        if (cardsZone.right - cardsZone.left > 0 && cardsZone.bottom - cardsZone.top > 0) {
                            allRectCanBeCardZone.add(cardsZone);
                        }
                        cardsZone = new Rect();
                        cardsZone.left = -1;
                    }
                }else {
                    if (Color.red(pixel) >= patternRedColor) {
                        followPath(cardsZone, patternRedColor, false, coverImageIntArray1D, bitmap.getWidth(), bitmap.getHeight(), j, i);
//                        cardsZone.right+=1;
                        if (cardsZone.right - cardsZone.left > 0 && cardsZone.bottom - cardsZone.top > 0) {
                            allRectCanBeCardZone.add(cardsZone);
                        }
                        cardsZone = new Rect();
                        cardsZone.left = -1;
                    }
                }
            }
        }


        return allRectCanBeCardZone;

    }

    public List<Rect> getRectsMathPattern(int[] pixels,int width,int height,int patternRedColor,boolean lessThanPattern){
        List<Rect> allRectCanBeCardZone = new ArrayList<>();
        Rect cardsZone = new Rect();
        cardsZone.left =-1;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = pixels[j + i *width];
                if(lessThanPattern) {
                    if (Color.red(pixel) < patternRedColor) {
                        followPath(cardsZone, patternRedColor, true, pixels,width, height, j, i);
//                        cardsZone.right+=1;
                        allRectCanBeCardZone.add(cardsZone);
                        cardsZone = new Rect();
                        cardsZone.left = -1;
                    }
                }else {
                    if (Color.red(pixel) >= patternRedColor) {
                        followPath(cardsZone, patternRedColor, false, pixels, width,height, j, i);
//                        cardsZone.right+=1;
                        allRectCanBeCardZone.add(cardsZone);
                        cardsZone = new Rect();
                        cardsZone.left = -1;
                    }
                }
            }
        }


        return allRectCanBeCardZone;

    }





}
