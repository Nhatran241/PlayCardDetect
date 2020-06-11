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
//    private FirebaseVisionTextRecognizer detector;
//    private FirebaseVisionImageLabeler labeler;
//    private FirebaseVisionObjectDetector objectDetector;
//    private   FirebaseAutoMLRemoteModel remoteModel;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        initServer();
        initPlayerData();
//        initExtractText();
//        initCustomLabel();
        requestCapture();
        initObjectDetection();
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

            long start = System.currentTimeMillis();
//        int [] allpixels = new int [bitmap.getHeight() * bitmap.getWidth()];
//
//        bitmap.getPixels(allpixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int startx = -1,starty=-1;
        int stopx=-1,stopy=-1;
        for (int i = 0; i <bitmap.getHeight() ; i++) {
            for (int j = 0; j <bitmap.getWidth() ; j++) {
                int colour = bitmap.getPixel(j, i);

                int red = Color.red(colour);
                int blue = Color.blue(colour);
                int green = Color.green(colour);
//                Log.d(TAG, "onBitmapReady: "+red+"/"+green+"/"+blue);
                if(255-red<30&&255-blue<30&&255-green<30){
                    bitmap.setPixel(j,i,Color.rgb(0,0,0));
                }else {

                }
            }
        }
//        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap1, startx, starty, stopx-startx, stopy-starty);

//        Log.d(TAG, "onBitmapReady: "+croppedBitmap.getWidth()+"/"+croppedBitmap.getHeight());
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator+"test";
        String fileType = "PNG";
        try {
            File file = SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(this, bitmap, System.currentTimeMillis()+"", filePath, fileType);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            bitmap.recycle();
        }

//        if(bitmap.getWidth()>bitmap.getHeight()){
//            CardsManager.getInstance().process(bitmap, this, new CardsManager.OnCardSplistListener() {
//                @Override
//                public void OnCardSplistCompleted(final List<Bitmap> cards) {
//                    for (int i = 0; i <cards.size() ; i++) {
//                        InputImage image = InputImage.fromBitmap(cards.get(i), 0);
//                        objectDetector
//                                .process(image)
//                                .addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        Log.d("nhatnhat", "onFailure: "+e.toString());
//                                    }
//                                })
//                                .addOnSuccessListener(new OnSuccessListener<List<DetectedObject>>() {
//                                    @Override
//                                    public void onSuccess(List<DetectedObject> results) {
//                                        for (DetectedObject detectedObject : results) {
//                                            Rect boundingBox = detectedObject.getBoundingBox();
//                                            Integer trackingId = detectedObject.getTrackingId();
//                                            for (DetectedObject.Label label : detectedObject.getLabels()) {
//                                                String text = label.getText();
//                                                int index = label.getIndex();
//                                                float confidence = label.getConfidence();
//                                                Log.d("nhatnhat", "onSuccess: "+boundingBox.centerX()+"/"+text);
//                                            }
//                                        }
//                                    }
//                                });
//
//                }}
//
//            });
//        }else {
//            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator;
//            String fileType = "PNG";
//            try {
//                File file = SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(this, bitmap, "wlh", filePath, fileType);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            Log.d("nhatnhat", "onBitmapReady: hotiziontal");
//        }


    }
}
