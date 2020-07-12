package com.machinelearning.playcarddetect.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.machinelearning.playcarddetect.data.Cons;


public class RemoteService extends AccessibilityService {
    private int number_clicker;
    private Handler mHandler;
    private int clicker_now = 0;
    private Intent broadcast =new Intent();
    @Override
    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getStringExtra(Cons.Action);
            if (Cons.RemoteClick.equals(action)) {
                Log.d("AutoService", action);
                int[] position = intent.getIntArrayExtra(Cons.RemoteClick);
//                    if (mRunnable == null) {
//                        mRunnable = new IntervalRunnable();
//                    }
//                    mHandler.postDelayed(mRunnable, 0);
                    click(position);
            } else if (Cons.RemoteSwipe.equals(action)) {
//                mHandler.removeCallbacksAndMessages(null);
//                Toast.makeText(getBaseContext(), "停止", Toast.LENGTH_SHORT).show();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    private void click(int[] position) {
        Path path = new Path();
        path.moveTo(position[0], position[1]);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 10L, 10L));
        GestureDescription gestureDescription = builder.build();                    Toast.makeText(getBaseContext(), "Click"+position[0]+"|"+position[1], Toast.LENGTH_SHORT).show();

        dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
//                broadcast.setAction(Cons.ClickSuccess);
//                sendBroadcast(broadcast);
//                Log.d("nhatnhat", "onCompleted: clicked");
                Toast.makeText(RemoteService.this, "Comple", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);

                Toast.makeText(RemoteService.this, "Fail"+gestureDescription.toString(), Toast.LENGTH_SHORT).show();
//                Log.d("nhatnhat", "onFail"+gestureDescription.toString());
            }
        }, null);
    }

    private void swipe(){
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        int middleYValue = displayMetrics.heightPixels / 2;
        final int leftSideOfScreen = displayMetrics.widthPixels / 4;
        final int rightSizeOfScreen = leftSideOfScreen * 3;
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        if (clicker_now%2==0) {
            //Swipe left
            path.moveTo(rightSizeOfScreen, middleYValue);
            path.lineTo(leftSideOfScreen, middleYValue);
        } else {
            //Swipe right
            path.moveTo(leftSideOfScreen, middleYValue);
            path.lineTo(rightSizeOfScreen, middleYValue);
        }

        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 50));
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                clicker_now += 1;
//                if(clicker_now==number_clicker){
//                    clicker_now = 0;
//                }
                mHandler.postDelayed(mRunnable, (long) 1000);
            }
        }, null);
    }
    private IntervalRunnable mRunnable;

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
//            playTap();
            swipe();
        }
    }


}
