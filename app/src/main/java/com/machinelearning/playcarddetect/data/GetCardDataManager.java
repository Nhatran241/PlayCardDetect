package com.machinelearning.playcarddetect.data;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import com.machinelearning.playcarddetect.data.model.Card;

import java.util.ArrayList;
import java.util.List;

public class GetCardDataManager {
    private static final int BITMAPWIDTH = 720;
    private static GetCardDataManager instance;

    public static GetCardDataManager getInstance() {
        if(instance==null)
            instance = new GetCardDataManager();
        return instance;
    }

    private String checkNumber(Bitmap cardBitmap,Rect rectNumberInCard, int patternColor, boolean lessThan) {
        Bitmap numberBitmap = Bitmap.createBitmap(cardBitmap, rectNumberInCard.left, rectNumberInCard.top, (rectNumberInCard.right - rectNumberInCard.left)+1, (rectNumberInCard.bottom - rectNumberInCard.top)+1);
        int width = numberBitmap.getWidth();
        int height = numberBitmap.getHeight();
        int[] pixelsOfNumber = new int[numberBitmap.getWidth() * numberBitmap.getHeight()];
        numberBitmap.getPixels(pixelsOfNumber, 0, numberBitmap.getWidth(), 0, 0, numberBitmap.getWidth(), numberBitmap.getHeight());
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
                int pixel = pixelsOfNumber[j + i * width];
                if(lessThan) {
                    if (Color.red(pixel) <patternColor){
                        row+= "0";
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

                        row+= "1";
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
            Log.d("numberview", ": "+row);
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

        List<Rect> rects = getRectsMathPattern(pixelsOfNumber,width,height,patternColor,false);
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
                int pixelFirstColum = pixelsOfNumber[listMatch.get(0)[0] + i * width];
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
                int pixelLastColum = pixelsOfNumber[listMatch.get(0)[1] + i * width];
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
                int pixelMinLeftColum = pixelsOfNumber[minLeft + i * width];
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
                int pixelMaxColum = pixelsOfNumber[max + i * width];
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

    private Card.Suit checkSuit(Bitmap cardBitmap,Rect rectSuitInCard, int patternColor, boolean lessThan){
        Bitmap suitBitmap = Bitmap.createBitmap(cardBitmap, rectSuitInCard.left, rectSuitInCard.top, (rectSuitInCard.right - rectSuitInCard.left)+1, (rectSuitInCard.bottom - rectSuitInCard.top)+1);
        int width = suitBitmap.getWidth();
        int height = suitBitmap.getHeight();
        int[] pixelsOfSuit = new int[suitBitmap.getWidth() * suitBitmap.getHeight()];
        suitBitmap.getPixels(pixelsOfSuit, 0, suitBitmap.getWidth(), 0, 0, suitBitmap.getWidth(), suitBitmap.getHeight());
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
                int pixel = pixelsOfSuit[j + i * width];
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



        List<Rect> rects = getRectsMathPattern(pixelsOfSuit,width,height,patternColor,false);
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
                    return Card.Suit.NotDetect;
                }
            }
        }
        if(isCo){
            return Card.Suit.Co;
        }
        int preTbc=0;
        int maxDif=0;
        for (int i = 0; i <width; i++) {
            int tbc=0;
            for (int j = 0; j < height/2; j++) {
                int pixel = pixelsOfSuit[i + j * width];
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
                    return Card.Suit.Chuon;
                }else {
                    boolean isBich=false;
                    int preCount=0;
                    for (int i = height-1; i >height/2 ; i--) {
                        int cout=0;
                        for (int j = 0; j <width ; j++) {
                            int pixel = pixelsOfSuit[j + i * width];
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
                        return Card.Suit.Bich;
                    }else {
                        return Card.Suit.Ro;
                    }
                }
        }
        return Card.Suit.NotDetect;
    }
    public List<Card> getCardsZoneBitmap(Bitmap baseBitmap , Rect zoneContainCardsZone,int patternCard,int patternZone){
        List<Card> cardsInZone = new ArrayList<>();
        /**
         * Lấy tất cả Rect của lá bài trong bitmap ban đầu
         */
        List<Rect> getCards = getRectsMathPattern(baseBitmap,patternZone,false);
        String number ="";
        Card.Suit suitType = Card.Suit.NotDetect;
        for (Rect cardRect:getCards) {
            /**
             * Nếu là lá bài thì chiều dài và rộng phải lớn hơn ít 10 rất nhiều
             */
            if(cardRect.right-cardRect.left>=10 && cardRect.bottom-cardRect.top>10) {

                number = "";
                suitType = Card.Suit.NotDetect;
                /**
                 * Cắt nhỏ từng lá bài ra từ rect của chúng
                 */
                Bitmap cardBitmap = Bitmap.createBitmap(baseBitmap, cardRect.left, cardRect.top, (cardRect.right - cardRect.left)+1, (cardRect.bottom - cardRect.top)+1);
                /**
                 * Dựa vào lá bài vừa cắt tiếp tục chạy thuật toán để lấy các giá trị và hệ của lá bài
                 */
                List<Rect> numberAndSuit = getRectsMathPattern(cardBitmap,patternCard,true);
                if(numberAndSuit.size()>0) {
                    outsideloop:
                    for (int i = 0; i <numberAndSuit.size() ; i++) {
                            Rect numberAndSuitRect = numberAndSuit.get(i);
                            if(i==0){
                                /**
                                 * Giá trị của lá bài (Level) luôn năm ở vị trí đầu tiên trong list các Rect lấy đc từ lá bài
                                 */
                                number =checkNumber(cardBitmap,numberAndSuitRect,patternCard,true);
                                Log.d("checksuitsssss", "getCardsZoneBitmap: "+number);
                            }
                            else {
                                if(!number.equals("Not Detected")&&!number.equals("")){
                                    suitType =checkSuit(cardBitmap,numberAndSuitRect,patternCard,true);
                                    if(suitType!= Card.Suit.NotDetect){
                                        Card card = new Card();
                                        card.setCardLevel(number);
                                        card.setCardRect(cardRect);
                                        card.setCardsuit(suitType);
                                        cardsInZone.add(card);
                                        break outsideloop;
                                    }
                                }
                            }
                    }

                }
            }
        }
        return cardsInZone;


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
                    if (Color.red(pixel) <= patternRedColor&&pixel!=-1) {
                        followPath(cardsZone, patternRedColor, true, coverImageIntArray1D, bitmap.getWidth(), bitmap.getHeight(), j, i);
//                        cardsZone.right+=1;
                        if (cardsZone.right - cardsZone.left > 0 && cardsZone.bottom - cardsZone.top > 0) {
                            allRectCanBeCardZone.add(cardsZone);
                        }
                        cardsZone = new Rect();
                        cardsZone.left = -1;
                    }
                }else {
                    if (Color.red(pixel) >= patternRedColor&&pixel!=-1) {
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
