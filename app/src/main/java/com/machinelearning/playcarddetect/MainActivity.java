package com.machinelearning.playcarddetect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity implements CaptureManager.onBitmapListener {
    /**
     * Define
     */
    public static String TAG ="nhatnhat";
    private static final int REQUESTCAPTURE = 1111 ;
    /**
     *
     */
    private CaptureManager captureManager;
    private TextRecognizer recognizer;

    private BroadcastReceiver takeBitmapOnTime;
    private FirebaseFirestore db;
    private ObjectDetector objectDetector;


    private WindowManager mWindowManager;
    private View overlayIcon,imagerect;
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
        initObjectDetection();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            // Show alert dialog to the user saying a separate permission is needed
            // Launch the settings activity if the user prefers
            Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(myIntent);
        }
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayIcon = LayoutInflater.from(this).inflate(R.layout.bubble_layout, null);
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
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            } else {
                params_rect = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
            }

        }
        mWindowManager.addView(overlayIcon,params);
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

    private void initObjectDetection() {
        LocalModel localModel =
                new LocalModel.Builder()
                        .setAssetFilePath("model.tflite")
                        // or .setAbsoluteFilePath(absolute file path to tflite model)
                        .build();
        CustomObjectDetectorOptions customObjectDetectorOptions =
                new CustomObjectDetectorOptions.Builder(localModel)
                        .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()
                        .setClassificationConfidenceThreshold(0.5f)
                        .setMaxPerObjectLabelCount(3)
                        .build();
        objectDetector =
                ObjectDetection.getClient(customObjectDetectorOptions);
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

        },0,4000);//Update text every second

    }




    private void requestCapture() {
        captureManager = new CaptureManager();
        captureManager.requestScreenshotPermission(this,REQUESTCAPTURE);
        captureManager.setOnGrantedPermissionListener(new CaptureManager.onGrantedPermissionListener() {
            @Override
            public void onResult(boolean isGranted) {
                if(isGranted){
                    captureManager.init(MainActivity.this);
//                    initTimer();
                }else {
                    captureManager.requestScreenshotPermission(MainActivity.this,REQUESTCAPTURE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        captureManager.onActivityResult(resultCode,data);
        if(requestCode==101&&resultCode==RESULT_OK){
            mWindowManager.addView(overlayIcon,params);
            overlayIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    captureManager.takeScreenshot();
                }
            });
        }
    }
    private byte[][] conver1dTo2d(byte[] arr, int w, int h){
        byte[][] answer = new byte[h][w];
        for(int i = 0 ; i<arr.length; i++){
            answer[i/w][i%w] = arr[i];
        }
        return answer;
    }
    @Override
    public void onBitmapReady(final Bitmap bitmap) {
//        InputImage image = InputImage.fromBitmap(Bitmap.createBitmap(bitmap,0,bitmap.getHeight()/2,bitmap.getWidth(),bitmap.getHeight()/2), 0);
        float ratio = bitmap.getWidth()/bitmap.getHeight();
        Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 720, (int) (720/ratio), false);

        InputImage image = InputImage.fromBitmap(bitmap1,0);
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                List<Card> list = new ArrayList<>();
                                int x=0,y=0,height=0;
                                for (int i = 0; i <visionText.getTextBlocks().size() ; i++) {
                                    for (Text.Line line :visionText.getTextBlocks().get(i).getLines()
                                         ) {
                                        for (Text.Element element:
                                             line.getElements()) {
                                            Rect textRect = element.getBoundingBox();
                                            if(x==0)
                                                x=textRect.left;
                                            String cardName = element.getText().trim();
                                            if(isCard(bitmap1,textRect)) {
                                                if (cardName.length() == 1) {
//                                                    if (cardName.equals("A") || cardName.equals("2") || cardName.equals("3") ||
//                                                            cardName.equals("4") || cardName.equals("5") || cardName.equals("6") ||
//                                                            cardName.equals("7") || cardName.equals("8") || cardName.equals("9") ||
//                                                            cardName.equals("10") || cardName.equals("J") || cardName.equals("Q") ||
//                                                            cardName.equals("K") || cardName.equals("1") || cardName.equals("0")) {
                                                        String suit = checkSuitOfCard(textRect, bitmap1, cardName);
//                                                    }
                                                } else {
                                                    int length = cardName.length();
                                                    int rectW = textRect.right - textRect.left;
                                                    for (int j = 0; j < length; j++) {
                                                        Rect rect = new Rect();
                                                        rect.left = textRect.left + (rectW / length) * i;
                                                        rect.right = rect.left + (rectW / length) * i;
                                                        rect.top = textRect.top;
                                                        rect.bottom = textRect.bottom;
                                                        checkSuitOfCard(rect, bitmap1, cardName.charAt(j) + "");
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: "+e.toString());
                                    }
                                });





    }

    private boolean isCard(Bitmap bitmap1, Rect textRect) {
        int[] coverImageIntArray1D = new int[bitmap1.getWidth() * bitmap1.getHeight()];
        bitmap1.getPixels(coverImageIntArray1D, 0, bitmap1.getWidth(),
                0, 0, bitmap1.getWidth(), bitmap1.getHeight());

        int pixelleft = coverImageIntArray1D[textRect.left + textRect.centerY()*bitmap1.getWidth()];
        int pixelright = coverImageIntArray1D[textRect.right + textRect.centerY()*bitmap1.getWidth()];
        int pixeltop = coverImageIntArray1D[textRect.centerX() + textRect.top*bitmap1.getWidth()];
        int pixelbot = coverImageIntArray1D[textRect.centerX() + textRect.bottom*bitmap1.getWidth()];
        int redl = Color.red(pixelleft);
        int redr = Color.red(pixelright);
        int redt = Color.red(pixeltop);
        int redb = Color.red(pixelbot);
        if(redl>210||redr>210||redt>210||redb>210)
            return true;
        return false;
    }

    private String checkSuitOfCard(Rect rect,Bitmap bitmap,String cardname) {

        int maxleft = rect.centerX() - 10;
        int maxright = rect.centerX() + 10;

        if (rect.bottom + 1 + maxright - maxleft > bitmap.getHeight())
            return "Not a card";

        if (rect.right + maxright - maxleft > bitmap.getWidth())
            return "Not a card";
        Bitmap smallBitmap = bitmap.createBitmap(bitmap, maxleft, rect.bottom + 1, maxright - maxleft, maxright - maxleft);
//        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "nhatnhatnhat";
//        try {
//            SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(this,smallBitmap,cardname,filePath,"PNG");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        int[] coverImageIntArray1D = new int[smallBitmap.getWidth() * smallBitmap.getHeight()];
        smallBitmap.getPixels(coverImageIntArray1D, 0, smallBitmap.getWidth(),
                0, 0, smallBitmap.getWidth(), smallBitmap.getHeight());

        // Co
        int firstMatchFirstRow = 0, lastMatchLastRow = 0;
        int firstRow = 0;
        int countMatchInFisrtRow = 0;
        int countMatchInMaxRow = 0;
        //Ro
        int maxRowPixelCount = 0;
        int columHeight = 0;
        boolean inObjectRow = false;

        for (int i = 0; i < smallBitmap.getHeight(); i++) {
            int maxMatchInRow = 0;
            String pixelinRow = "";
            int rowPixelCount = 0;
            for (int j = 0; j < smallBitmap.getWidth(); j++) {
                int pixel = coverImageIntArray1D[j + i * smallBitmap.getWidth()];
                int red = Color.red(pixel);
                String reds = red + "";
                if (red < 100)
                    reds = 0 + "" + red;
                pixelinRow += reds + "   ";
                if (red < 100) {
                    columHeight++;
                    //Diamond
                    inObjectRow = true;
                    //Hearth
                    rowPixelCount++;
                    if (firstMatchFirstRow == 0) {
                        firstMatchFirstRow = j;
                        firstRow = i;
                    }
                    maxMatchInRow++;
                    if (i == firstRow)
                        countMatchInFisrtRow++;

                }


            }
            if (maxMatchInRow > countMatchInMaxRow)
                countMatchInMaxRow = maxMatchInRow;
            if (inObjectRow) {
                if (rowPixelCount >= maxRowPixelCount) {
                    columHeight++;
                } else {
                    columHeight--;
                }
                maxRowPixelCount = rowPixelCount;
            }

            inObjectRow = false;


            Log.d(TAG, "checkSuitOfCard: " + pixelinRow + "\n");
        }
        Log.d(TAG, "checkSuitOfCard: "+cardname+" "+columHeight);
        return "";
//        if (countMatchInFisrtRow >= countMatchInMaxRow / 2) {
//            Log.d(TAG, "checkSuitOfCard: " + cardname + "is Hearth"+countMatchInFisrtRow+"/"+countMatchInMaxRow);
//            return "Hearth";
//        } else {
//            if (countMatchInFisrtRow >= 3) {
//                Log.d(TAG, "checkSuitOfCard:" + cardname + "is Club");
//                return "Club";
//            } else {
//                if (Math.abs(columHeight) < 4) {
//                    Log.d(TAG, "checkSuitOfCard:" + cardname + "is Diamond");
//                    return "Diamond";
//                } else {
//                    Log.d(TAG, "checkSuitOfCard:" + cardname + "is Spade");
//                    return "Spade";
//                }
//            }
//        }
//        if(Math.abs(lastMatchLastRow-firstMatchFirstRow)>=5){
//            Log.d(TAG, "checkSuitOfCard:"+cardname+"is Hearth");
//            return"Hearth";
//        }else if(Math.abs(lastMatchLastRow-firstMatchFirstRow)<=3){
//
//        if(Math.abs(columHeight)<4){
//            Log.d(TAG, "checkSuitOfCard:"+cardname+"is Diamond");
//            return "Diamond";
//        }else {
//            Log.d(TAG, "checkSuitOfCard:"+cardname+"is Spade");
//            return "Spade";
//        }
//        }else {
//            Log.d(TAG, "checkSuitOfCard:"+cardname+"is Club");
//            return "Club";
//        }
    }

    public List<Bitmap> splistCardsFromBitmap(int numcard,int startX,int startY,int cardW,int cardH,Bitmap bitmap){
        List<Bitmap> cardsBitmap = new ArrayList<>();
        for (int i = 0; i <numcard ; i++) {
            Bitmap bitmap1 = Bitmap.createBitmap(bitmap,startX+cardW*i,startY,cardW,cardH);
            cardsBitmap.add(bitmap1);
        }


        return cardsBitmap;
    }

}
