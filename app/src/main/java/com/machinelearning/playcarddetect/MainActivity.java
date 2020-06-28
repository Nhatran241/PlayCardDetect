package com.machinelearning.playcarddetect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.machinelearning.playcarddetect.data.BitmapsAdapter;
import com.machinelearning.playcarddetect.data.card.Card;
import com.machinelearning.playcarddetect.data.card.Suit;
import com.machinelearning.playcarddetect.process.GetCardDataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CaptureManager.onBitmapListener {
    private static final int OVERLAY = 1234;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(myIntent,OVERLAY);
            }else {

                requestCapture();
            }
        }

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
                    chatHeadView();
//                    initTimer();
                } else {
                    captureManager.requestScreenshotPermission(MainActivity.this, REQUESTCAPTURE);
                }
            }
        });
    }

    private void chatHeadView() {
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
        params.gravity = Gravity.RIGHT|Gravity.TOP;
        imageoverlay.setVisibility(View.GONE);
        overlayIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureManager.takeScreenshot();
            }
        });
        captureManager.init(MainActivity.this);
        mWindowManager.addView(overlayIcon, params);
        mWindowManager.addView(imageoverlay, params_rect);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==OVERLAY){
            if (Settings.canDrawOverlays(this)) {
                requestCapture();
            }
        }
        if(requestCode==REQUESTCAPTURE){
            captureManager.onActivityResult(resultCode, data);
        }

//        if (requestCode == 101 && resultCode == RESULT_OK) {
//            mWindowManager.addView(overlayIcon, params);
//            overlayIcon.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    captureManager.takeScreenshot();
//                }
//            });
//        }
    }

    @Override
    public void onBitmapReady(final Bitmap bitmap) {
        Rect handCardsZone = new Rect();
        handCardsZone.top = 250;
        handCardsZone.left= 160;
        handCardsZone.right=600;
        handCardsZone.bottom = 360;

        Rect currentCardsTableZone = new Rect();
        currentCardsTableZone.top = 100;
        currentCardsTableZone.left= 130;
        currentCardsTableZone.right=580;
        currentCardsTableZone.bottom = 220;

        List<Bitmap> list = new ArrayList<>();
        long start = System.currentTimeMillis();
        list.addAll(GetCardDataManager.getInstance().getCardsZoneBitmap(bitmap,handCardsZone,240,200));
//        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "Auto";;
//        for (int i = 0; i <list.size() ; i++) {
//            try {
//                SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(this,list.get(i),"Card"+i,path,"PNG");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        list.clear();
        list.addAll(GetCardDataManager.getInstance().getCardsZoneBitmap(bitmap,currentCardsTableZone,220,200));
//        for (int i = 0; i <list.size() ; i++) {
//            try {
//                SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(this,list.get(i),"Card"+i+"Suit",path,"PNG");
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        Log.d(TAG, "time: "+(System.currentTimeMillis()-start));
        BitmapsAdapter adapter = new BitmapsAdapter(list);
                    recyclerView.setAdapter(adapter);
                    imageoverlay.setVisibility(View.VISIBLE);

    }






}
