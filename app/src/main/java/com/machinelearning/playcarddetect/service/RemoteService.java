package com.machinelearning.playcarddetect.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Path;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;


import com.machinelearning.playcarddetect.service.model.CustomPath;
import com.machinelearning.playcarddetect.service.model.RemoteProfile;

import java.util.List;


public class RemoteService extends AccessibilityService {
    private Handler mHandler;
    private Intent broadcast =new Intent();

    /**
     *
     */
    RemoteProfile profile;

    /**
     * Click
     */
    List<CustomPath> pathListWhenClick;

    @Override
    protected void onServiceConnected() {
        Log.d("nhatnhat", "onServiceConnected: ");
        super.onServiceConnected();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("nhatnhat", "onCreated: ");
        HandlerThread handlerThread = new HandlerThread("auto-handler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.d("nhatnhat", "onStartCommand: ");
            profile = (RemoteProfile) intent.getSerializableExtra(Cons.REMOTEPROFILE);
            if(profile!=null){
                Log.d("nhatnhat", "onStartCommand1: ");
                switch (profile.getRemoteType()){
                    case CLICK:{
                        Log.d("nhatnhat", "onStartCommand2: ");
                        if (mRunnable == null) {
                            mRunnable = new IntervalRunnable();
                        }
                        pathListWhenClick = profile.getPathList();
                        if(pathListWhenClick.size()==0){
                            Toast.makeText(this, "No paths to click", Toast.LENGTH_SHORT).show();
                        }else {
                            if (profile.getLoopTime()!=0&&profile.getLoopTime() < pathListWhenClick.size()) {
                                pathListWhenClick.subList(pathListWhenClick.size() - profile.getLoopTime(), pathListWhenClick.size()).clear();
                            }
                            mHandler.postDelayed(mRunnable, profile.getDelayOnStart());
                        }
                        break;
                    }
                }
            }else {
                Toast.makeText(this, "Remote Profile must not be null", Toast.LENGTH_SHORT).show();
            }
//            if (Cons.RemoteClick.equals(action)) {
//                clickPosition = intent.getIntArrayExtra(Cons.RemoteClick);
//                    if(clickPosition==null){
//                        Toast.makeText(this, "Action_Click need position int[x,y]", Toast.LENGTH_SHORT).show();
//                    }else {
//                        if (mRunnable == null) {
//                            mRunnable = new IntervalRunnable();
//                        }
//                        mHandler.postDelayed(mRunnable, click_delay);
//                    }
//            } else if (Cons.RemoteSwipe.equals(action)) {
//            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    private void click() {

        if(pathListWhenClick.size()==0){
            Toast.makeText(this, "Click completed", Toast.LENGTH_SHORT).show();
            return;
        }

        GestureDescription.Builder builder = new GestureDescription.Builder();
        final Path path = pathListWhenClick.get(0);
        builder.addStroke(new GestureDescription.StrokeDescription( path, 0, profile.getDuration()));
        GestureDescription gestureDescription = builder.build();
        boolean result =dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                pathListWhenClick.remove(0);
                mHandler.postDelayed(mRunnable, (long) profile.getDelayOnLoop());
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d("nhatnhat", "onStartCommand: "+gestureDescription.toString());
            }
        }, null);
        Log.d("nhatnhat", "onStartCommand:3"+result);

    }

//    private void swipe(){
//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
//
//        int middleYValue = displayMetrics.heightPixels / 2;
//        final int leftSideOfScreen = displayMetrics.widthPixels / 4;
//        final int rightSizeOfScreen = leftSideOfScreen * 3;
//        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
//        Path path = new Path();
//
//        if (clicker_now%2==0) {
//            //Swipe left
//            path.moveTo(rightSizeOfScreen, middleYValue);
//            path.lineTo(leftSideOfScreen, middleYValue);
//        } else {
//            //Swipe right
//            path.moveTo(leftSideOfScreen, middleYValue);
//            path.lineTo(rightSizeOfScreen, middleYValue);
//        }
//
//        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 50));
//        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
//            @Override
//            public void onCompleted(GestureDescription gestureDescription) {
//                super.onCompleted(gestureDescription);
//                clicker_now += 1;
////                if(clicker_now==number_clicker){
////                    clicker_now = 0;
////                }
//                mHandler.postDelayed(mRunnable, (long) 1000);
//            }
//        }, null);
//    }
    private IntervalRunnable mRunnable;

    private class IntervalRunnable implements Runnable {
        @Override
        public void run() {
            click();
//            playTap();
//            swipe();
        }
    }


}
