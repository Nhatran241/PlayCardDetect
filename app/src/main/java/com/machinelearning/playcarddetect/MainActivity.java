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
                                            String cardName = element.getText().trim().toString();
                                            if(cardName.equals("A")||cardName.equals("2")||cardName.equals("3")||
                                                    cardName.equals("4")||cardName.equals("5")||cardName.equals("6")||
                                                    cardName.equals("7")||cardName.equals("8")||cardName.equals("9")||
                                                    cardName.equals("10")||cardName.equals("J")||cardName.equals("Q")||
                                                    cardName.equals("K")||cardName.equals("1")||cardName.equals("0")){

                                                String suit = checkSuitOfCard(textRect, bitmap1, cardName);
                                                list.add(new Card(cardName, suit));
                                            }

//                                            Log.d(TAG, ""+element.getText()+" width"+(textRect.right-textRect.left)+" centerx "+textRect.centerX());
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

    private String checkSuitOfCard(Rect rect,Bitmap bitmap,String cardname) {
        int maxleft=rect.centerX()-10;
        int maxright=rect.centerX()+10;

        Log.d(TAG, "checkSuitOfCard: "+cardname);
        Bitmap smallBitmap = bitmap.createBitmap(bitmap,maxleft,rect.bottom+1,maxright-maxleft,maxright-maxleft);
//        suits.add(smallBitmap);
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "graysclae";
        try {
            SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(this,smallBitmap,cardname,filePath,"PNG");
        } catch (Exception e) {
            e.printStackTrace();
        }
        int[] coverImageIntArray1D = new int[smallBitmap.getWidth() * smallBitmap.getHeight()];
        smallBitmap.getPixels(coverImageIntArray1D, 0, smallBitmap.getWidth(),
                0, 0, smallBitmap.getWidth(), smallBitmap.getHeight());

        int firstMatch=0,lastMatch=0; // Same row
        int maxMatch=0;
        for (int i = 0; i <smallBitmap.getHeight() ; i++) {
            int maxMatchInRow=0;
            String pixelinRow="";
            for (int j = 0; j <smallBitmap.getWidth() ; j++) {
                int pixel = coverImageIntArray1D[j + i*smallBitmap.getWidth()];
                int red = Color.red(pixel);
                int blue = Color.blue(pixel);
                int green = Color.green(pixel);
                String reds=red+"",blues=blue+"",greens=green+"";
                if(red<100)
                    reds=0+""+red;

                if(blue<100)
                    blues=0+""+blue;

                if(green<100)
                    greens=0+""+green;
                pixelinRow+=reds+"   ";
                if(red<150&&blue<150&&green<150){
                    maxMatchInRow++;
                    if(firstMatch==0) {
                        firstMatch = j;
                    }else {
                        if(j>lastMatch)

                            lastMatch =j;
                    }
                    if(maxMatchInRow>maxMatch)
                        maxMatch = maxMatchInRow;
                }


            }
            Log.d(TAG, "checkSuitOfCard: "+pixelinRow +"\n");
        }

        if(lastMatch-firstMatch>maxMatch*7/10){
        }
        return "";
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
