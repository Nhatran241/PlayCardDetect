package com.machinelearning.playcarddetect.ui;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.machinelearning.playcarddetect.data.CaptureManager;
import com.machinelearning.playcarddetect.R;
import com.machinelearning.playcarddetect.data.model.Card;
import com.machinelearning.playcarddetect.data.GetCardDataManager;
import com.machinelearning.playcarddetect.server.ClientServerManager;
import com.machinelearning.playcarddetect.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ClientActivity extends BaseActivity implements CaptureManager.onBitmapListener {
    private static final int OVERLAY = 1234;
    private static final int REQUESTCAPTURE = 1111;
    private CaptureManager captureManager;
    private WindowManager mWindowManager;
    private View overlayIcon, imageoverlay;
    private RecyclerView recyclerView;
    private RelativeLayout rect;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams params_rect;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    @Override
    protected void PrepareServer() {
        showDialogLoading();
        ClientServerManager.getInstance().prepareClientServer(this,false, new ClientServerManager.IClientPrepareListener() {
            @Override
            public void OnPrepareClientServerSuccess() {
                dismisDialogLoading();
                /**
                 * Chuẩn bị quá trình chụp ảnh
                 */
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(ClientActivity.this)) {
                        Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivityForResult(myIntent,OVERLAY);
                    }else {
                        requestCapture();
                    }
                }
            }

            @Override
            public void OnPrepareClientServerFail(String error) {
                Toast.makeText(ClientActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                dismisDialogLoading();
            }

        });

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
                    captureManager.requestScreenshotPermission(ClientActivity.this, REQUESTCAPTURE);
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
        captureManager.init(ClientActivity.this);
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
        /**
         * Phân tích lá bài từ ảnh chụp
         */
        Rect cardsInHandZone = new Rect();
//        currentCardsTableZone.top = 170;
//        currentCardsTableZone.left= 150;
//        currentCardsTableZone.right=580;
//        currentCardsTableZone.bottom = 270;

//        showDialogLoading();
        List<Card> listCardsInHand = new ArrayList<>();
        long start = System.currentTimeMillis();
        listCardsInHand.addAll(GetCardDataManager.getInstance().getCardsZoneBitmap(bitmap,cardsInHandZone,220,230));
        Log.d("nhatnhat", "time: "+(System.currentTimeMillis()-start));

        /**
         * Đẩy dữ liệu lên server
         */
        ClientServerManager.getInstance().putClientHandCards(listCardsInHand, new ClientServerManager.IClientPutValueListener() {
            @Override
            public void OnClientPutValueSuccess() {
//                dismisDialogLoading();
                Log.d("nhatnhat", "OnClientPutValueSuccess: ");
            }

            @Override
            public void OnClientPutValueFail(String error) {
//                dismisDialogLoading();

                Log.d("nhatnhat", "OnClientPutValueFail: "+error.toString());
            }
        });



    }



}
