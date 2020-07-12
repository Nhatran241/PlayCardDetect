package com.machinelearning.playcarddetect.server;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.machinelearning.playcarddetect.data.CaptureManager;
import com.machinelearning.playcarddetect.data.SaveImageUtil;

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
        if(recognizer ==null)
            recognizer = TextRecognition.getClient();
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        recognizer.process(image)
                .addOnSuccessListener(new OnSuccessListener<Text>() {
                    @Override
                    public void onSuccess(Text visionText) {
                        String text =visionText.getText().toLowerCase();
                        Log.d("ocrcann", "onSuccess: "+text);
                        if(text.contains("tao ban")||text.contains("solo")||text.contains("tap su")||text.contains("trieu phu")){

                        for (Text.TextBlock block : visionText.getTextBlocks()) {
                            String blockText = block.getText();
                            Point[] blockCornerPoints = block.getCornerPoints();
                            Rect blockFrame = block.getBoundingBox();
                            for (Text.Line line : block.getLines()) {
                                String lineText = line.getText();
                                Point[] lineCornerPoints = line.getCornerPoints();
                                Rect lineFrame = line.getBoundingBox();
                                for (Text.Element element : line.getElements()) {
                                    String elementText = element.getText().toLowerCase();
                                    Point[] elementCornerPoints = element.getCornerPoints();
                                    Rect elementFrame = element.getBoundingBox();
                                    Log.d("nhatnhat", "onSuccess: "+bitmap.getWidth()+":"+bitmap.getHeight()+"|"+elementText+"/"+elementFrame.left+":"+elementFrame.bottom);
//                                    if(elementText.contains("solo")){
//                                        int[] postionTaoBanButton = new int[2];
//                                        postionTaoBanButton[0] = (int) elementFrame.exactCenterX();
//                                        postionTaoBanButton[1] = (int) elementFrame.exactCenterY();
//                                        clientManagerListener.OnCurrentPosition(CurrentPosition.RoomSelect,postionTaoBanButton);
//                                    }
//                                    if(elementText.contains("dong y")){
//                                        int[] postionTaoBanButton = new int[2];
//                                        postionTaoBanButton[0] = (int) elementFrame.exactCenterX();
//                                        postionTaoBanButton[1] = (int) elementFrame.exactCenterY();
//                                        clientManagerListener.OnCurrentPosition(CurrentPosition.RoomSelect,postionTaoBanButton);
//                                    }
                                    if(elementText.contains("muc cuoc")&&elementText.contains("xin doi")){
                                        int[] postionTaoBanButton = new int[2];
                                        postionTaoBanButton[0] = 10;
                                        postionTaoBanButton[1] = 10;
                                        clientManagerListener.OnCurrentPosition(CurrentPosition.RoomSelect,postionTaoBanButton);
                                    }
                                }
                            }
                        }
                        }else {
                            clientManagerListener.OnCurrentPosition(CurrentPosition.Undetected,null);
                        }
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
        void OnCurrentPosition(CurrentPosition currentPosition,int[] postionClick);
    }
}