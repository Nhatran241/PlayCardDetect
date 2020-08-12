package com.machinelearning.playcarddetect.modules.client.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.machinelearning.playcarddetect.common.model.CardBase64;
import com.machinelearning.playcarddetect.modules.datamanager.CaptureManager;
import com.machinelearning.playcarddetect.modules.datamanager.CardCollectionManager;
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager;
import com.machinelearning.playcarddetect.modules.datamanager.TextCollectionManager;

import java.util.ArrayList;
import java.util.List;


public class ClientService extends AccessibilityService implements CaptureManager.onBitmapListener {
    public static boolean isConnected = false;
    private Handler mHandler;
    private Intent broadcast = new Intent();
    private CaptureManager captureManager;

    /**
     * Data
     */
    private List<CardBase64> listCardInHand = new ArrayList<>();
    private float scaleRatio;
    private int newHeight;
    private int newWidth;
    private int screenWidth;
    private int screenHeight;
    private TextCollectionManager.CurrentPosition currentPosition = TextCollectionManager.CurrentPosition.Undetected;

    /**
     * Click
     */

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        showForegroundNotificationMode1();
        final Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point size = new Point();
        display.getRealSize(size);
//            if(width<height) {
        if (size.x < size.y) {
            screenWidth = size.y;
            screenHeight = size.x;
        } else {
            screenWidth = size.x;
            screenHeight = size.y;
        }
        isConnected = true;
        captureManager = CaptureManager.getInstance();
        captureManager.setListener(this);
        ServerClientDataManager.getInstance().prepareClientServer(this, false, new ServerClientDataManager.IClientPrepareListener() {
            @Override
            public void OnPrepareClientServerSuccess() {
                ServerClientDataManager.getInstance().RegisterClientListenerWithServer(new ServerClientDataManager.IClientListener() {
                    @Override
                    public void OnServerClickCard(int position) {
                        currentPosition = TextCollectionManager.CurrentPosition.PLaying;
                        Rect cardRect = listCardInHand.get(position).getCardRect();
                        int y = newHeight - (newHeight / 3) + cardRect.top + cardRect.centerY();
                        int x = cardRect.centerX(); //512/1024
                        // ? /1570
                        x *= (double) screenWidth / newWidth;
                        y *= (double) screenHeight / newHeight;
                        click(x, y);

                    }

                    @Override
                    public void OnServerClickXepBai() {

                    }
                });

            }

            @Override
            public void OnPrepareClientServerFail(String error) {
                Toast.makeText(ClientService.this, "" + error, Toast.LENGTH_SHORT).show();
            }

        });
    }

    @NonNull
    @TargetApi(Build.VERSION_CODES.O)
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "snap map fake location ";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel("snap map channel", name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return "snap map channel";
    }

    private void showForegroundNotificationMode1() {

        String channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            channel = createChannel();
        else {
            channel = "";
        }


        // Create intent that will bring our app to the front, as if it was tapped in the app

        Notification notification = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            notification = new Notification.Builder(getApplicationContext(), channel)
                    .setContentTitle("Running")
                    .build();
        } else {

            notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Running")
                    .build();
        }
        startForeground(1, notification);
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

//        if (intent != null) {
//            Log.d("nhatnhat", "onStartCommand: ");
//            profile = (RemoteProfile) intent.getSerializableExtra(Cons.REMOTEPROFILE);
//            if(profile!=null){
//                Log.d("nhatnhat", "onStartCommand1: ");
//                switch (profile.getRemoteType()){
//                    case CLICK:{
//                        Log.d("nhatnhat", "onStartCommand2: ");
//                        if (mRunnable == null) {
//                            mRunnable = new IntervalRunnable();
//                        }
//                        pathListWhenClick = profile.getPathList();
//                        if(pathListWhenClick.size()==0){
//                            Toast.makeText(this, "No paths to click", Toast.LENGTH_SHORT).show();
//                        }else {
//                            if (profile.getLoopTime()!=0&&profile.getLoopTime() < pathListWhenClick.size()) {
//                                pathListWhenClick.subList(pathListWhenClick.size() - profile.getLoopTime(), pathListWhenClick.size()).clear();
//                            }
//                            mHandler.postDelayed(mRunnable, profile.getDelayOnStart());
//                        }
//                        break;
//                    }
//                }
//            }else {
//                Toast.makeText(this, "Remote Profile must not be null", Toast.LENGTH_SHORT).show();
//            }
////            if (Cons.RemoteClick.equals(action)) {
////                clickPosition = intent.getIntArrayExtra(Cons.RemoteClick);
////                    if(clickPosition==null){
////                        Toast.makeText(this, "Action_Click need position int[x,y]", Toast.LENGTH_SHORT).show();
////                    }else {
////                        if (mRunnable == null) {
////                            mRunnable = new IntervalRunnable();
////                        }
////                        mHandler.postDelayed(mRunnable, click_delay);
////                    }
////            } else if (Cons.RemoteSwipe.equals(action)) {
////            }
//        }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    @Override
    public void onInterrupt() {

    }

    private void click(int x, int y) {
        GestureDescription.Builder builder = new GestureDescription.Builder();
        final Path path = new Path();
        path.moveTo(x, y);
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
        GestureDescription gestureDescription = builder.build();
        boolean result = dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                Toast.makeText(ClientService.this, "Click compeleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                Log.d("nhatnhat", "onStartCommand: " + gestureDescription.toString());
            }
        }, null);
        Log.d("nhatnhat", "onStartCommand:3" + result);

    }


    @Override
    public void onBitmapReady(Bitmap bitmap) {

        if (bitmap != null) {
            if(currentPosition.equals(TextCollectionManager.CurrentPosition.PLaying)){
                prepareAndPutData(bitmap);
            }else {
                checkRoomNumber(bitmap);
            }
        } else {
            captureManager.takeScreenshot();
        }
    }

    private void checkRoomNumber(Bitmap bitmap) {
        Bitmap bitmapForOcr = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth() / 4, bitmap.getHeight() / 4);
        TextCollectionManager.getInstance().process(bitmapForOcr, (currentPosition, postionClick) -> {
            bitmapForOcr.recycle();
            if (currentPosition == TextCollectionManager.CurrentPosition.PLaying) {
                prepareAndPutData(bitmap);
            } else captureManager.takeScreenshot();
        });
        }

    private void prepareAndPutData(Bitmap bitmap) {
        Rect cardsInHandZone = new Rect();
        List<CardBase64> listCardsInHand = new ArrayList<>();
        /**
         * Chỉnh Bitmap về kích thước hợp lý nhất
         */
        scaleRatio = bitmap.getWidth() * 1f / bitmap.getHeight() * 1f;
        newHeight = bitmap.getHeight();
        newWidth = bitmap.getWidth();
        if (scaleRatio > 0) {
            newWidth = 1024;
            newHeight = (int) (newHeight / scaleRatio);
        }
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
        /**
         * Cắt nhỏ Bitmap về khu vực lá bài trên tay
         */
        Bitmap handCardsBitmap = Bitmap.createBitmap(newBitmap, 0, newHeight - (newHeight / 3), newWidth, newHeight / 3);
        bitmap.recycle();
        newBitmap.recycle();

        listCardsInHand.addAll(CardCollectionManager.getInstance().getCardsZoneBitmap(this, handCardsBitmap, cardsInHandZone, 220, 230));
        handCardsBitmap.recycle();
        if (listCardsInHand.size() > 0) {
//                        if(listCardInHand.size() ==0){
//                            listCardInHand.addAll(listCardsInHand);
//                            ServerClientDataManager.getInstance().putClientHandCards(listCardInHand, new ServerClientDataManager.IClientPutValueListener() {
//                                @Override
//                                public void OnClientPutValueSuccess() {
//                                    captureManager.takeScreenshot();
//                                }
//
//                                @Override
//                                public void OnClientPutValueFail(String error) {
//                                    Toast.makeText(ClientService.this, ""+error, Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }else {
            if (listCardsInHand.size() == listCardInHand.size()) {
//                                boolean notMatch=false;
//                                for (int i = 0; i <listCardsInHand.size() ; i++) {
//                                    if(!listCardsInHand.get(i).getCardRect().equals(listCardInHand.get(i).getCardLevel())||
//                                            !listCardsInHand.get(i).getCardsuit().equals(listCardInHand.get(i).getCardsuit())||
//                                            !listCardsInHand.get(i).getCardRect().equals(listCardInHand.get(i).getCardRect())){
//                                        notMatch=true;
//                                        break;
//                                    }
//                                }
//                                if(notMatch){
//                                    ServerClientDataManager.getInstance().putClientHandCards(listCardInHand, new ServerClientDataManager.IClientPutValueListener() {
//                                        @Override
//                                        public void OnClientPutValueSuccess() {
//                                            captureManager.takeScreenshot();
//                                        }
//
//                                        @Override
//                                        public void OnClientPutValueFail(String error) {
//                                            Toast.makeText(ClientService.this, "" + error, Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                }else {
                captureManager.takeScreenshot();
//                                }

            } else {
                listCardInHand.clear();
                listCardInHand.addAll(listCardsInHand);
                ServerClientDataManager.getInstance().putClientHandCards(listCardInHand, new ServerClientDataManager.IClientPutValueListener() {
                    @Override
                    public void OnClientPutValueSuccess() {
                        captureManager.takeScreenshot();
                    }

                    @Override
                    public void OnClientPutValueFail(String error) {
                        Toast.makeText(ClientService.this, "" + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
    }else captureManager.takeScreenshot();

    }


}
