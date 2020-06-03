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
import com.machinelearning.playcarddetect.data.Card;
import com.machinelearning.playcarddetect.data.CardsManager;
import com.machinelearning.playcarddetect.reciver.TakeBitmapOnTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

    /**
     * Player data
     */
    private List<Card> playercards;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPlayerData();
        initExtractText();
        initCustomLabel();
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

    private void initPlayerData() {
        playercards = new ArrayList<>();
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

    private void initTimer() {

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
    private void getTextFromImage(final FirebaseVisionImage card, final List<Bitmap> suit) {
        Task<FirebaseVisionText> result =
                detector.processImage(card)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                /**
                                 * Extract text from result
                                 */
                                for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
                                    for (FirebaseVisionText.Line line: block.getLines()) {
                                        for (final FirebaseVisionText.Element element: line.getElements()) {
                                            Log.d("nhatnhat", "------------------------");
                                            if(element.getText().length()>1){
//                                                int width =(element.getBoundingBox().right-element.getBoundingBox().left)/element.getText().length();
//                                                Log.d("nhatnhat", "onSuccess: "+element.getText());
//                                                for (int i = 0; i <element.getText().length() ; i++) {
//                                                    String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator;
//                                                    Bitmap bitmap1 = Bitmap.createBitmap(card.getBitmap(),element.getBoundingBox().left+(width*i),element.getBoundingBox().bottom-10,50,50);
//                                                    String fileType = "PNG";
//                                                    try {
//                                                        File file = SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(MainActivity.this, bitmap1, element.getText().charAt(i)+"", filePath, fileType);
//                                                    } catch (Exception e) {
//                                                        e.printStackTrace();
//                                                    }
//                                                    getLabelFromCustom(createFirebaseVisionImage(bitmap1),element.getText().charAt(i)+"");
//
//                                                }
                                            }else {
                                                Log.d("nhatnhat", "onSuccess: "+element.getText());
                                                String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator;
                                                final Bitmap bitmap1 = Bitmap.createBitmap(card.getBitmap(), (int) (element.getBoundingBox().exactCenterX()-16),60,32,32);
                                                String fileType = "PNG";
                                                try {
                                                    File file = SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(MainActivity.this, bitmap1, element.getText(), filePath, fileType);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }

                                                getLabelFromCustom(createFirebaseVisionImage(bitmap1),element.getText());

                                            }

                                        }
                                    }
                                }

//                                if(blocks.size()>3) {
//                                    for (int i = 0; i <blocks.size() ; i++) {
//                                        Log.d("nhatnhat", "block :: "+blocks.get(i).getText());
//                                    }
//                                    String playercard = blocks.get(blocks.size()-3).getText();
//                                    for (int i = 0; i <playercard.length() ; i++) {
//                                        if(playercard.charAt(i)!=' '){
//                                            boolean hasone =false;
//                                            for (Card card:playercards) {
//                                                if(card.getCardLevel().equals(playercard.charAt(i)+"")){
//                                                    hasone=true;
//                                                }
//                                            }
//                                            if(!hasone&&playercards.size()<13)
//                                                playercards.add(new Card(playercard.charAt(i)+"",""));
//                                        }
//                                    }
//                                    Log.d(TAG, "playercard:---------------------------------------- ");
//                                    for (int i = 0; i <playercards.size() ; i++) {
//                                        Log.d(TAG, "playercard: "+playercards.get(i).getCardLevel());
//                                    }
////
//                                }else {
//                                    Toast.makeText(MainActivity.this, "No View To Detect", Toast.LENGTH_SHORT).show();
//                                }
//                                String resultText = firebaseVisionText.getText();
//                                for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {
//                                    String blockText = block.getText();
//                                    Float blockConfidence = block.getConfidence();
//                                    List<RecognizedLanguage> blockLanguages = block.getRecognizedLanguages();
//                                    Point[] blockCornerPoints = block.getCornerPoints();
//                                    Rect blockFrame = block.getBoundingBox();
//
////                                    for (FirebaseVisionText.Line line: block.getLines()) {
////                                        String lineText = line.getText();
////                                        Float lineConfidence = line.getConfidence();
////                                        List<RecognizedLanguage> lineLanguages = line.getRecognizedLanguages();
////                                        Point[] lineCornerPoints = line.getCornerPoints();
////                                        Rect lineFrame = line.getBoundingBox();
////                                        Log.d("nhatnhat", "line text: "+lineText+" location :"+lineFrame.top);
////
////
////                                        Bitmap bitmap = image.getBitmap();
////                                        if(bitmap.getHeight()>bitmap.getWidth()){
////                                            bitmap = RotateBitmap(bitmap,90);
////                                        }
////                                        Log.d("nhatnhat", "new calculation"+bitmap.getHeight()+"/"+bitmap.getWidth());
////                                        for (FirebaseVisionText.Element element: line.getElements()) {
////                                            String elementText = element.getText();
////                                            Float elementConfidence = element.getConfidence();
////                                            List<RecognizedLanguage> elementLanguages = element.getRecognizedLanguages();
////                                            Point[] elementCornerPoints = element.getCornerPoints();
////                                            if(elementText.equals("A")||elementText.equals("2")||elementText.equals("3")||elementText.equals("4")
////                                                    ||elementText.equals("5")||elementText.equals("6")||elementText.equals("7")||elementText.equals("8")
////                                                    ||elementText.equals("9")||elementText.equals("10")||elementText.equals("J")||elementText.equals("K")
////                                                    ||elementText.equals("Q")) {
////                                                Rect elementFrame = element.getBoundingBox();
////                                                if(bitmap.getHeight()>bitmap.getWidth()) {
////
////                                                }else {
////                                                    if(element.getBoundingBox().top<bitmap.getHeight()/2){
////                                                        /**
////                                                         * Player cards
////                                                         */
//////                                                        Bitmap card = Bitmap.createBitmap(bitmap,elementFrame.left,elementFrame.bottom, 50, 50);
//////                                                        ImageView imageView = findViewById(R.id.iv_main);
//////                                                        imageView.setImageBitmap(bitmap);
//////                                                        getLabelFromCustom(createFirebaseVisionImage(card),elementText);
////                                                        Log.d("nhatnhat", "player card"+elementText+" :"+"y="+element.getBoundingBox().top);
////
////                                                    }else if(element.getBoundingBox().top<bitmap.getHeight()*3/4&&element.getBoundingBox().top>bitmap.getHeight()/4){
////                                                        /**
////                                                         * Table cards
////                                                         */
////                                                        Log.d("nhatnhat", "table card"+elementText);
////
//////                                                        Bitmap card = Bitmap.createBitmap(bitmap,elementFrame.left,elementFrame.bottom, 50, 50);
//////                                                        ImageView imageView = findViewById(R.id.iv_main);
//////                                                        imageView.setImageBitmap(bitmap);
//////                                                        getLabelFromCustom(createFirebaseVisionImage(card),elementText);
////                                                    }
////                                                }
////
//////                                                    }
//////                                                }
//////                                                rect.setVisibility(View.VISIBLE);
//////                                                ImageView view = new ImageView(MainActivity.this);
//////                                                RelativeLayout.LayoutParams params;
//////                                                params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//////                                                params.leftMargin = elementFrame.left;
//////                                                params.topMargin = elementFrame.top;
////////                                            Bitmap card;
//////                                                Log.d("customlabel", "-----------------");
//////
//////                                                Log.d("customlabel", "onSuccess: "+bitmap.getWidth()+"/"+bitmap.getHeight());
//////                                                Log.d("customlabel", "onSuccess: "+elementFrame.left+":"+elementFrame.bottom);
////
//////                                                rect.addView(view, params);
////                                            }
////                                        }
////                                    }
//                                }

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("nhatnhat", "onFailure: "+e.toString());
                                    }
                                });
    }
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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
                .setConfidenceThreshold(0.6f)  // Evaluate your model in the Firebase console
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
                                Log.d("nhatnhat", "suit of: "+text+" is:"+a.getText());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("nhatnhat", "checking suit for "+text+" failed" +e.toString());
                        }
                    });
        } catch (FirebaseMLException e) {
            e.printStackTrace();
            Log.d("nhatnhat", "fail: "+e.toString());
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

    private FirebaseVisionImage createFirebaseVisionImage(Bitmap bitmap) {
        return FirebaseVisionImage.fromBitmap(bitmap);
    }

    public File savebitmap(Bitmap bmp) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 60, bytes);
        File f = new File(Environment.getExternalStorageDirectory()
                + File.separator + "testimage.jpg");
        f.createNewFile();
        FileOutputStream fo = new FileOutputStream(f);
        fo.write(bytes.toByteArray());
        fo.close();
        return f;
    }
    @Override
    public void onBitmapReady(Bitmap bitmap) {
        if(bitmap.getWidth()>bitmap.getHeight()){
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator;
            String fileType = "PNG";
            try {
                File file = SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(this, bitmap, "wlh", filePath, fileType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            CardsManager.getInstance().process(bitmap, this, new CardsManager.OnCardSplistListener() {
                @Override
                public void OnCardSplistCompleted(List<Bitmap> cards,List<Bitmap> suit) {
                    for (int i = 0; i <cards.size() ; i++) {
                        FirebaseVisionImage card = createFirebaseVisionImage(cards.get(i));
                        getTextFromImage(card,suit);
                    }

                }

            });
        }else {
            String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator;
            String fileType = "PNG";
            try {
                File file = SaveImageUtil.getInstance().saveScreenshotToPicturesFolder(this, bitmap, "wlh", filePath, fileType);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("nhatnhat", "onBitmapReady: hotiziontal");
        }


//            FirebaseVisionImage image = createFirebaseVisionImage(card);
//            getTextFromImage(image);
//            ImageView imageView = findViewById(R.id.iv_main);
//            imageView.setImageBitmap(bitmap);

    }
}
