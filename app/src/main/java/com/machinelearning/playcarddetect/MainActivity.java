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
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.automl.FirebaseAutoMLRemoteModel;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.google.firebase.ml.vision.objects.FirebaseVisionObject;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetector;
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.firebase.ml.vision.text.RecognizedLanguage;
import com.machinelearning.playcarddetect.reciver.TakeBitmapOnTime;

import java.util.List;
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
    private FirebaseVisionTextRecognizer detector;
    private FirebaseVisionImageLabeler labeler;
    private FirebaseVisionObjectDetector objectDetector;
    private   FirebaseAutoMLRemoteModel remoteModel;
    private BroadcastReceiver takeBitmapOnTime;


    private WindowManager mWindowManager;
    private View overlayIcon,imagerect;
    private RelativeLayout rect;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams params_rect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initExtractText();
        initLabelObject();
        initCustomLabel();
        initTrackObject();
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayIcon = LayoutInflater.from(this).inflate(R.layout.bubble_layout, null);
        rect= (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.rect, null);
        rect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rect.setVisibility(View.GONE);
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

//            params_rect.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
//        initTimer();
    }

    private void initCustomLabel() {

         remoteModel =
                new FirebaseAutoMLRemoteModel.Builder("playcards_202052922811").build();
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        requestCapture();
                    }
                });
    }

    private void initTrackObject() {

        FirebaseVisionObjectDetectorOptions options =
                new FirebaseVisionObjectDetectorOptions.Builder()
                        .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                        .enableMultipleObjects()
                        .enableClassification()  // Optional
                        .build();
         objectDetector =
                FirebaseVision.getInstance().getOnDeviceObjectDetector(options);
    }

    private void initLabelObject() {
        labeler = FirebaseVision.getInstance()
                .getOnDeviceImageLabeler();
    }

    private void initTimer() {
//        if (android.os.Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {   //Android M Or Over
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
//            startActivityForResult(intent, 2000);
//        }else {
//            mWindowManager.addView(overlayIcon,params);
//            mWindowManager.addView(rect,params_rect);
//            overlayIcon.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    captureManager.takeScreenshot();
//                }
//            });
//        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
              captureManager.takeScreenshot();
            }

        },0,4000);//Update text every second

    }



    private void initExtractText() {
        FirebaseApp.initializeApp(this);
        detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
    }

    private void requestCapture() {
        captureManager = new CaptureManager();
        captureManager.requestScreenshotPermission(this,REQUESTCAPTURE);
        captureManager.setOnGrantedPermissionListener(new CaptureManager.onGrantedPermissionListener() {
            @Override
            public void onResult(boolean isGranted) {
                if(isGranted){
                    captureManager.init(MainActivity.this);
                    initTimer();
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
        if(requestCode==2000&&resultCode==RESULT_OK){
            mWindowManager.addView(overlayIcon,params);
            Toast.makeText(this, "asdasd", Toast.LENGTH_SHORT).show();
            overlayIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    captureManager.takeScreenshot();
                }
            });
        }
    }

    private void getTextFromImage(final FirebaseVisionImage image) {
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                /**
                                 * Extract text from result
                                 */
                                Log.d(TAG, "onSuccess: extrat");
                                String resultText = firebaseVisionText.getText();
                                for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                                    String blockText = block.getText();
                                    Float blockConfidence = block.getConfidence();
                                    List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
                                    Point[] blockCornerPoints = block.getCornerPoints();
                                    Rect blockFrame = block.getBoundingBox();

                                    for (FirebaseVisionText.Line line: block.getLines()) {
                                        String lineText = line.getText();
                                        Float lineConfidence = line.getConfidence();
                                        List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
                                        Point[] lineCornerPoints = line.getCornerPoints();
                                        Rect lineFrame = line.getBoundingBox();


                                        Log.d("text", "onSuccess: "+lineText);
                                        Bitmap bitmap = image.getBitmap();
                                        for (FirebaseVisionText.Element element: line.getElements()) {
                                            String elementText = element.getText();
                                            Float elementConfidence = element.getConfidence();
                                            List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
                                            Point[] elementCornerPoints = element.getCornerPoints();
                                            if(elementText.equals("A")||elementText.equals("2")||elementText.equals("3")||elementText.equals("4")
                                                    ||elementText.equals("5")||elementText.equals("6")||elementText.equals("7")||elementText.equals("8")
                                                    ||elementText.equals("9")||elementText.equals("10")||elementText.equals("J")||elementText.equals("K")
                                                    ||elementText.equals("Q")) {
                                                Rect elementFrame = element.getBoundingBox();
//                                                if(bitmap.getHeight()>bitmap.getWidth()) {
                                                    if (element.getBoundingBox().top < bitmap.getWidth()/2){
                                                        Bitmap card = Bitmap.createBitmap(bitmap,elementFrame.left,elementFrame.bottom, 50, 50);
                                                        ImageView imageView = findViewById(R.id.iv_main);
                                                        imageView.setImageBitmap(bitmap);
                                                        getLabelFromCustom(createFirebaseVisionImage(card),elementText);
                                                    }
//                                                }
//                                                rect.setVisibility(View.VISIBLE);
//                                                ImageView view = new ImageView(MainActivity.this);
//                                                RelativeLayout.LayoutParams params;
//                                                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                                                params.leftMargin = elementFrame.left;
//                                                params.topMargin = elementFrame.top;
////                                            Bitmap card;
//                                                Log.d("customlabel", "-----------------");
//
//                                                Log.d("customlabel", "onSuccess: "+bitmap.getWidth()+"/"+bitmap.getHeight());
//                                                Log.d("customlabel", "onSuccess: "+elementFrame.left+":"+elementFrame.bottom);

//                                                rect.addView(view, params);
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
                                        // Task failed with an exception
                                        // ...
                                    }
                                });
    }
    public Bitmap[][] splitBitmap(Bitmap bitmap, int xCount, int yCount) {
        // Allocate a two dimensional array to hold the individual images.
        Bitmap[][] bitmaps = new Bitmap[xCount][yCount];
        int width, height;
        // Divide the original bitmap width by the desired vertical column count
        width = bitmap.getWidth() / xCount;
        // Divide the original bitmap height by the desired horizontal row count
        height = bitmap.getHeight() / yCount;
        // Loop the array and create bitmaps for each coordinate
        for(int x = 0; x < xCount; ++x) {
            for(int y = 0; y < yCount; ++y) {
                // Create the sliced bitmap
                bitmaps[x][y] = Bitmap.createBitmap(bitmap, x * width, y * height, width, height);
            }
        }
        // Return the array
        return bitmaps;
    }
    private void getLabelFromCustom(FirebaseVisionImage firebaseVisionImage, final String text){
        FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder optionsBuilder;
            optionsBuilder = new FirebaseVisionOnDeviceAutoMLImageLabelerOptions.Builder(remoteModel);
        FirebaseVisionOnDeviceAutoMLImageLabelerOptions options = optionsBuilder
                .setConfidenceThreshold(0.5f)  // Evaluate your model in the Firebase console
                // to determine an appropriate threshold.
                .build();

        FirebaseVisionImageLabeler labeler;
        try {
            labeler = FirebaseVision.getInstance().getOnDeviceAutoMLImageLabeler(options);
            labeler.processImage(firebaseVisionImage)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            for (FirebaseVisionImageLabel a:
                                    labels) {
                                Log.d("customlabel", "onSuccess: "+a.getText()+""+text);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("customlabel", "fail: "+e.toString());
                        }
                    });
        } catch (FirebaseMLException e) {
            e.printStackTrace();
            Log.d("customlabel", "fail: "+e.toString());
        }

    }
    private void getLabelFromImage(FirebaseVisionImage image) {
        labeler.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                        for (FirebaseVisionImageLabel a:
                             labels) {
                            Log.d("label", "onSuccess: "+a.getText());
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });
    }

    private void trackObjectFromImage(FirebaseVisionImage image){
        objectDetector.processImage(image)
                .addOnSuccessListener(
                        new OnSuccessListener<List<FirebaseVisionObject>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionObject> detectedObjects) {
                                Log.d("nhatnhattrack", "----------------------");
                                for (int i = 0; i <detectedObjects.size() ; i++) {
                                    Log.d("nhatnhattrack", detectedObjects.get(i).getClassificationConfidence()+"onSuccess: "+detectedObjects.get(i).getBoundingBox().centerX()+":"+detectedObjects.get(i).getBoundingBox().centerY());

                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                // ...
                            }
                        });
    }
    private FirebaseVisionImage createFirebaseVisionImage(Bitmap bitmap) {
        return FirebaseVisionImage.fromBitmap(bitmap);
    }

    @Override
    public void onBitmapReady(Bitmap bitmap) {
        Log.d(TAG, "onBitmapReady: ");
        FirebaseVisionImage image = createFirebaseVisionImage(bitmap);
        getTextFromImage(image);
//        getLabelFromImage(image);
//        getLabelFromCustom(image);
//        trackObjectFromImage(image);
        ImageView imageView = findViewById(R.id.iv_main);
        imageView.setImageBitmap(bitmap);
    }

}
