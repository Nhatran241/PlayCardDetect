package com.machinelearning.playcarddetect.server;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

public class ClientManager {
    private static ClientManager instance;
    private TextRecognizer recognizer ;

    public enum CurrentPosition {
        MenuGame,
        RoomSelect,
        InsideRoom,
        Disconnected,
        Undetected
    }
    public static ClientManager getInstance() {
        if(instance==null)
            instance = new ClientManager();
        return instance;
    }
    public void process(Bitmap bitmap,ClientManagerListener clientManagerListener){
        long start = System.currentTimeMillis();
        if(recognizer ==null)
            recognizer = TextRecognition.getClient();
        Log.d("nhatnhat", "start1: "+bitmap.getWidth()+"/"+bitmap.getHeight());
        Log.d("nhatnhat", "start1: "+(System.currentTimeMillis()-start));
        start = System.currentTimeMillis();
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        Log.d("nhatnhat", "start2: "+(System.currentTimeMillis()-start));
        start = System.currentTimeMillis();
        long finalStart = start;
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        Log.d("nhatnhat", "start3: "+(System.currentTimeMillis()- finalStart));
//                        Log.d("nhatnhat", "onSuccess: "+visionText.toString());
                        clientManagerListener.OnCurrentPosition(CurrentPosition.Undetected);
//                        for (Text.TextBlock block : visionText.getTextBlocks()) {
//                            String blockText = block.getText();
//                            Point[] blockCornerPoints = block.getCornerPoints();
//                            Rect blockFrame = block.getBoundingBox();
//                            for (Text.Line line : block.getLines()) {
//                                String lineText = line.getText();
//                                Point[] lineCornerPoints = line.getCornerPoints();
//                                Rect lineFrame = line.getBoundingBox();
//                                for (Text.Element element : line.getElements()) {
//                                    String elementText = element.getText().toLowerCase();
//                                    Point[] elementCornerPoints = element.getCornerPoints();
//                                    Rect elementFrame = element.getBoundingBox();
//                                    if(elementText.contains("bang xep hang")||elementText.contains("choi ngay")){
//                                        clientManagerListener.OnCurrentPosition(CurrentPosition.MenuGame);
//                                    }else if(elementText.contains("chon ban")||elementText.contains("tao ban")||elementText.contains("solo")){
//                                        clientManagerListener.OnCurrentPosition(CurrentPosition.RoomSelect);
//                                    }else clientManagerListener.OnCurrentPosition(CurrentPosition.Undetected);
//                                }
//                            }
//                        }
                    }
                })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });





    }
    public interface ClientManagerListener{
        void OnCurrentPosition(CurrentPosition currentPosition);
    }
}
