package com.machinelearning.playcarddetect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.google.common.collect.BiMap;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.machinelearning.playcarddetect.data.BitmapsAdapter;
import com.machinelearning.playcarddetect.data.card.Card;
import com.machinelearning.playcarddetect.data.card.Level;
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
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
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
            }else {

                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivityForResult(myIntent,OVERLAY);
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
        handCardsZone.top = 300;
        handCardsZone.left= 190;
        handCardsZone.right=550;
        handCardsZone.bottom = 480;

        Rect currentCardsTableZone = new Rect();
        currentCardsTableZone.top = 170;
        currentCardsTableZone.left= 150;
        currentCardsTableZone.right=580;
        currentCardsTableZone.bottom = 270;

        List<Card> list = new ArrayList<>();
        long start = System.currentTimeMillis();
//        list.addAll(GetCardDataManager.getInstance().getCardsZoneBitmap(bitmap,handCardsZone,240,150));
        list.addAll(GetCardDataManager.getInstance().getCardsZoneBitmap(bitmap,currentCardsTableZone,220,230));

        List<Bitmap> bitmaps = new ArrayList<>();
        bitmaps.add(bitmap);

        String path =Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "playingcard";
        for (int i = 0; i <list.size() ; i++) {
            // Create bitmap
            if(list.get(i).getCardLevel()!=null) {
                bitmaps.add(list.get(i).getCardLevel().getBitmap());
            }
            if(list.get(i).getCardsuit()!=null) {
                bitmaps.add(list.get(i).getCardsuit().getBitmap());
            }
//            Level level = list.get(i).getCardLevel();
//            Bitmap blevel = Bitmap.createBitmap(level.getWidth(), level.getHeight(), Bitmap.Config.ARGB_8888);
//            bitmap.setPixels(level.getPixel(), 0, level.getWidth(), 0, 0, level.getWidth(), level.getHeight());
//            bitmaps.add(blevel);
//
//            Suit suit = list.get(i).getCardsuit();
//            Bitmap bsuit = Bitmap.createBitmap(suit.getWidth(), suit.getHeight(), Bitmap.Config.ARGB_8888);
//            bitmap.setPixels(suit.getPixels(), 0, suit.getWidth(), 0, 0, suit.getWidth(), suit.getHeight());
//            bitmaps.add(bsuit);
        }


        Log.d(TAG, "onBitmapReady: "+list.size()+"/"+bitmaps.size());
        Log.d(TAG, "time: "+(System.currentTimeMillis()-start));
        BitmapsAdapter adapter = new BitmapsAdapter(bitmaps);
                    recyclerView.setAdapter(adapter);
                    imageoverlay.setVisibility(View.VISIBLE);

    }






}
