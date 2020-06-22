package com.machinelearning.playcarddetect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.machinelearning.playcarddetect.data.Card;
import com.machinelearning.playcarddetect.data.CardsManager;
import com.machinelearning.playcarddetect.data.MyAdapter;
import com.machinelearning.playcarddetect.reciver.TakeBitmapOnTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements CaptureManager.onBitmapListener {
    /**
     * Define
     */
    public static String TAG = "nhatnhat";
    private static final int REQUESTCAPTURE = 1111;
    /**
     *
     */
    private CaptureManager captureManager;
    private TextRecognizer recognizer;

    private BroadcastReceiver takeBitmapOnTime;
    private FirebaseFirestore db;
    private ObjectDetector objectDetector;


    private WindowManager mWindowManager;
    private View overlayIcon, imageoverlay;
    private RecyclerView recyclerView;
    private RelativeLayout rect;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams params_rect;

    /**
     * Player data
     */
    private List<Card> playercards;
    private List<Bitmap> suits;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initExtractText();
        initPlayerData();
        requestCapture();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        }
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayIcon = LayoutInflater.from(this).inflate(R.layout.bubble_layout, null);
        imageoverlay = LayoutInflater.from(this).inflate(R.layout.imageoverlay, null);
        Button button = imageoverlay.findViewById(R.id.btn_close);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageoverlay.setVisibility(View.GONE);
            }
        });
        recyclerView = imageoverlay.findViewById(R.id.rv_images);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        overlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureManager.takeScreenshot();
            }
        });

        if (params == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            } else {
                params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            }
        }
        if (params_rect == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                params_rect = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            } else {
                params_rect = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            }

        }
        mWindowManager.addView(overlayIcon, params);
        mWindowManager.addView(imageoverlay, params_rect);
        imageoverlay.setVisibility(View.GONE);
        overlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureManager.takeScreenshot();
            }
        });
    }

    private void initExtractText() {
        recognizer = TextRecognition.getClient();
    }


    private void initPlayerData() {
        playercards = new ArrayList<>();
        suits = new ArrayList<>();
    }


    private void initTimer() {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                captureManager.takeScreenshot();
            }

        }, 0, 4000);//Update text every second

    }


    private void requestCapture() {
        captureManager = new CaptureManager();
        captureManager.requestScreenshotPermission(this, REQUESTCAPTURE);
        captureManager.setOnGrantedPermissionListener(new CaptureManager.onGrantedPermissionListener() {
            @Override
            public void onResult(boolean isGranted) {
                if (isGranted) {
                    captureManager.init(MainActivity.this);
//                    initTimer();
                } else {
                    captureManager.requestScreenshotPermission(MainActivity.this, REQUESTCAPTURE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        captureManager.onActivityResult(resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            mWindowManager.addView(overlayIcon, params);
            overlayIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    captureManager.takeScreenshot();
                }
            });
        }
    }

    @Override
    public void onBitmapReady(final Bitmap bitmap) {
        float ratio = bitmap.getWidth() / bitmap.getHeight();
        Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 720, (int) (720 / ratio), false);

        InputImage image = InputImage.fromBitmap(bitmap1, 0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                int x = 0, y = 0, height = 0;

                                for (int i = 0; i < visionText.getTextBlocks().size(); i++) {
                                    Rect lineblockrect = visionText.getTextBlocks().get(i).getBoundingBox();
                                    for (Text.Line line : visionText.getTextBlocks().get(i).getLines()
                                    ) {
                                        for (Text.Element element :
                                                line.getElements()) {
                                            Rect textRect = element.getBoundingBox();
                                            String cardName = element.getText().trim();
                                            if (isCard(bitmap1, textRect)) {
                                                if (cardName.length() == 1) {
                                                    if (cardName.equals("A") || cardName.equals("2") || cardName.equals("3") ||
                                                            cardName.equals("4") || cardName.equals("5") || cardName.equals("6") ||
                                                            cardName.equals("7") || cardName.equals("8") || cardName.equals("9") ||
                                                            cardName.equals("10") || cardName.equals("J") || cardName.equals("Q") ||
                                                            cardName.equals("K") || cardName.equals("1") || cardName.equals("0")) {
                                                    Log.d(TAG, " x:"+textRect.centerX()+"y:"+textRect.centerY());

//                                                    textRect.top=lineblockrect.top;
//                                                    textRect.bottom=lineblockrect.bottom;
                                                    int h = (textRect.bottom-textRect.top)*2/3;
                                                    textRect.top=textRect.bottom+1;
                                                    textRect.left=textRect.centerX()-2;
                                                    textRect.right=textRect.centerX()+2;
                                                    textRect.bottom +=h;
                                                    checkSuitOfCard(textRect, bitmap1, cardName);
                                                    }
                                                } else {
                                                    int length = cardName.length();
                                                    int rectW = textRect.right - textRect.left;
                                                    for (int j = 0; j < length; j++) {
                                                        Rect rect = new Rect();
                                                        rect.left = textRect.left + ((rectW / length) * j);
                                                        rect.right = rect.left + ((rectW / length) * j);
                                                        rect.right = rect.right-(rect.right-rect.left)/2;
                                                        int h = (textRect.bottom-textRect.top)*2/3;
                                                        rect.top=textRect.bottom;
                                                        rect.bottom +=h;
                                                        checkSuitOfCard(rect, bitmap1, cardName.charAt(j) + "");
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                                recyclerView.setAdapter(new MyAdapter(suits));
                                imageoverlay.setVisibility(View.VISIBLE);
                            }

                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: " + e.toString());
                                    }
                                });


    }

    private Rect getSuitRect(Rect textRect) {
        Rect suitRect =new Rect();
        suitRect.top = textRect.top;
        suitRect.bottom = suitRect.top+40;
        suitRect.left= (int) (textRect.exactCenterX()-10);
        suitRect.right= (int) (textRect.exactCenterX()+10);
        return suitRect;

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

    private String checkSuitOfCard(Rect rect, Bitmap bitmap, String cardname) {


        if (rect.right-rect.left<=0)
            return "Not a card";

        if (rect.bottom-rect.top<=0)
            return "Not a card";

        Bitmap suitOnly = getSuitOnlyBitmap(rect,bitmap,cardname);
        suits.add(suitOnly);
        int[] coverImageIntArray1D = new int[suitOnly.getWidth() * suitOnly.getHeight()];
        suitOnly.getPixels(coverImageIntArray1D, 0, suitOnly.getWidth(),
                0, 0, suitOnly.getWidth(), suitOnly.getHeight());
        int[] firstMatchFirstRow = new int[2];
        int[] lastMatchChangeSideRow = new int[2];
        int[] lastMatchFirstRow = new int[2];
        int[] lastMatchLastRow = new int[2];
        int[] lastMatchMaxRow = new int[2];
        int maxRowCount;

        firstMatchFirstRow[0] = 0;
        firstMatchFirstRow[1] = 0;

        lastMatchFirstRow[0] = 0;
        lastMatchFirstRow[1] = 0;

        lastMatchLastRow[0] = 0;
        lastMatchLastRow[1] = 0;

        lastMatchChangeSideRow[0] = 0;
        lastMatchChangeSideRow[1] = 0;

        maxRowCount = 0;
        int previousRowPixelCount=0;
         int maxRowDifferentPixel =0;
        lastMatchMaxRow[0] = 0;
        lastMatchMaxRow[1] = 0;

        boolean isContinueUp=false;

        for (int i = 0; i < suitOnly.getHeight(); i++) {
            String pixelinRow = "";
            int maxInRow = 0;
            for (int j = 0; j < suitOnly.getWidth(); j++) {
                int pixel = coverImageIntArray1D[j + i * suitOnly.getWidth()];
                int red = Color.red(pixel);
                String reds = red + "";
                if (red < 100)
                    reds = 0 + "" + red;
                pixelinRow += reds + "   ";
                if (red < 150) {
                    maxInRow++;
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
            Log.d(TAG, ": "+maxInRow+"/"+previousRowPixelCount+"/"+maxRowDifferentPixel);
            if(maxInRow>=previousRowPixelCount) {
                int differentPixelCountInRow = maxInRow - previousRowPixelCount;
                previousRowPixelCount = maxInRow;
                if (differentPixelCountInRow > maxRowDifferentPixel)
                    maxRowDifferentPixel = differentPixelCountInRow;
            }
            if (maxInRow >= lastMatchMaxRow[0]) {
                isContinueUp =true;
                lastMatchMaxRow[0] = maxInRow;
                lastMatchMaxRow[1] = i;
            }else {
                if(isContinueUp){
                    //Điểm xoay chiều con Chuồn
                    if(lastMatchChangeSideRow[0]==0){
                        lastMatchChangeSideRow[0]=maxInRow;
                        lastMatchChangeSideRow[1]=i;
                    }
                }
            }



        }
        coverImageIntArray1D[lastMatchChangeSideRow[0] + lastMatchChangeSideRow[1] * suitOnly.getWidth()]=0;

        Log.d(TAG, "----------------------"+cardname);
        for (int i = 0; i < suitOnly.getHeight(); i++) {
            String pixelinRow = "";
            for (int j = 0; j < suitOnly.getWidth(); j++) {
                int pixel = coverImageIntArray1D[j + i * suitOnly.getWidth()];
                int red = Color.red(pixel);
                String reds = red + "";
                if (red < 100)
                    reds = 0 + "" + red;
                pixelinRow += reds + "   ";

            }

            Log.d(TAG, "checkSuitOfCard: " + pixelinRow + "\n");

        }
        Log.d(TAG, "----------------------"+cardname+"/" + maxRowDifferentPixel);
        int firstRowMatchCount = lastMatchFirstRow[0] - firstMatchFirstRow[0];
        if(firstRowMatchCount<3){
            // Có thể là rô và bích và chuồng
        }else {
            Log.d(TAG, ""+cardname +" là cở");
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
        return "";
    }

    private Bitmap getSuitOnlyBitmap(Rect rect, Bitmap bitmap,String cardname) {
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
//        if(rect.bottom+(rect.bottom-rect.top)>bitmap.getHeight())
//            return smallBitmap;
//        if(rect.right+(rect.right-rect.left)>bitmap.getWidth())
//            return smallBitmap;
        if(rect.left!=remainRect.left||rect.right!=remainRect.right||rect.top!=remainRect.top||rect.bottom!=remainRect.bottom){
            Log.d(TAG, cardname+" left"+rect.left+" right"+rect.right+" top"+rect.top+" bot"+rect.bottom);
             return getSuitOnlyBitmap(rect,bitmap,cardname);
        }else {
            return smallBitmap;
        }
    }

//    private Bitmap suitOnlyBitmap(Bitmap smallBitmap) {
//        int[] coverImageIntArray1D = new int[smallBitmap.getWidth() * smallBitmap.getHeight()];
//        smallBitmap.getPixels(coverImageIntArray1D, 0, smallBitmap.getWidth(),
//                0, 0, smallBitmap.getWidth(), smallBitmap.getHeight());
//        int[] firstLeftPixelInsideWhite = new int[2];
//        int[] lastLeftPixelInsideWhite = new int[2];
//        int[] lastRightPixelInsideWhite = new int[2];
//        int[] lastMaxPixelInsideWhite = new int[2];
//        firstLeftPixelInsideWhite[0] = 0;
//        firstLeftPixelInsideWhite[1] = 0;
//        lastLeftPixelInsideWhite[0] = 0;
//        lastLeftPixelInsideWhite[1] = 0;
//        lastRightPixelInsideWhite[0] = 0;
//        lastRightPixelInsideWhite[1] = 0;
//        lastMaxPixelInsideWhite[0] = 0;
//        lastMaxPixelInsideWhite[1] = 0;
//        int maxPixelRowCount = 0;
//        boolean isBlackContinue=false;
//        boolean previousIsBlack=false;
//        boolean isBlackInsideWhite=false;
//        for (int i = 0; i < smallBitmap.getHeight(); i++) {
//            int pixelRowCount = 0;
//            for (int j = 0; j < smallBitmap.getWidth(); j++) {
//                int pixel = coverImageIntArray1D[j + i * smallBitmap.getWidth()];
//                if (Color.red(pixel) > 150) {
//                    if (isBlackContinue)
//                        isBlackContinue = false;
//                    previousIsBlack = false;
//                } else {
//                    if (previousIsBlack)
//                        isBlackContinue = true;
//                    previousIsBlack = true;
//                }
//            }
//        }
//            Log.d(TAG, "left" + firstLeftPixelInsideWhite[0] + "right " + lastMaxPixelInsideWhite[0]);
//            smallBitmap = Bitmap.createBitmap(smallBitmap, lastMaxPixelInsideWhite[0] - maxPixelRowCount, firstLeftPixelInsideWhite[1],
//                    maxPixelRowCount, lastRightPixelInsideWhite[1] - firstLeftPixelInsideWhite[1]);
//            return smallBitmap;
//    }

}
