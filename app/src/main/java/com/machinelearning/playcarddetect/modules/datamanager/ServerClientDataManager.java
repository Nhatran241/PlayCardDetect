package com.machinelearning.playcarddetect.modules.datamanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.OpenApp;
import com.machinelearning.playcarddetect.common.model.CardBase64;
import com.nhatran241.accessibilityactionmodule.model.Action;
import com.nhatran241.accessibilityactionmodule.model.ClickAction;
import com.nhatran241.accessibilityactionmodule.model.SwipeAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerClientDataManager {
    private static ServerClientDataManager instance;
    public static String RESPONSE_SUCCESS = "success";
    public static String RESPONSE_FAIL = "failed";
    String remotePath ="Remote";
    String roomPath = "Room";
    String dataPath = "Data";
    String devicesPath = "Devices";
    private FirebaseFirestore db ;
    private String deviceId;
    private List<String> listDevice = new ArrayList<>();
    private Gson gson = new Gson();
    public static ServerClientDataManager getInstance() {
        if(instance==null)
            instance = new ServerClientDataManager();

        return instance;
    }


    @SuppressLint("HardwareIds")
    public void init(Context context){
        if(db == null)
            db = FirebaseFirestore.getInstance();
        deviceId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }
    /**
     * Admin function
     */

    public void AdminListenerToDataPath(IAdminListenerToDataPath iAdminListenerToDataPath){
        db.collection(dataPath).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                iAdminListenerToDataPath.onDataResponse("Admin_listener_data"+queryDocumentSnapshots,e+"");
            }
        });
    }
    public void AdminListenerToRoomPath(IAdminListenerToRoomPath iAdminListenerToRoomPath){
        db.collection(devicesPath).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Map<String,String> map = new HashMap<>();
                for (DocumentSnapshot a:queryDocumentSnapshots.getDocuments()) {
                    map.put(a.getId(),String.valueOf(a.get("currentroom")));
                }
                iAdminListenerToRoomPath.onRoom(map,e+"");
            }
        });
    }
    public void AdminPushRemote(Action action,String deviceIdListenerAction,IAdminPutRemoteCallback iAdminPutRemoteCallback){
        Map<String,Action> map = new HashMap<>();
        map.put("action",action);
        db.collection(remotePath).document(deviceId).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                iAdminPutRemoteCallback.onPushSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAdminPutRemoteCallback.onPushFailed(e.toString());
            }
        });
    }


    /**
     * Client function
     */
    
    public void ClientPushData(List<CardBase64> data,String deviceId,IClientCallbackToDataPath iClientCallbackToDataPath){  
        Map<String,CardBase64> map = new HashMap<>();
        for (int i = 0; i <data.size() ; i++) {
            map.put(i+"",data.get(i));
        }
        db.collection(dataPath).document(deviceId).set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                iClientCallbackToDataPath.onSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iClientCallbackToDataPath.onFailed(e.toString());
            }
        });
    }
    public void ClientPushRoom(String room,IClientCallbackToRoomPath iClientCallbackToRoomPath){
        final Map<String, String> currentroom = new HashMap<>();
        currentroom.put("currentroom",room);
        db.collection(devicesPath).document(deviceId).set(currentroom,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                iClientCallbackToRoomPath.onSuccess();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iClientCallbackToRoomPath.onFailed(e.toString());
            }
        });
    }
    public void ClientListenerToRemotePath(IClientListenerToRemotePath iClientListenerToRemotePath){
        db.collection(remotePath).document(deviceId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                assert documentSnapshot != null;
                String json = documentSnapshot.get("action").toString();
                Log.d("nhatnhat", "onEvent: "+json);
                if(json.contains("packagename")){
                    iClientListenerToRemotePath.onRemote(gson.fromJson(json,OpenApp.class));
                }else if(json.contains("gesture")){
                    if(json.contains("isSwipe")) {
                        iClientListenerToRemotePath.onRemote(gson.fromJson(json, SwipeAction.class));
                    }else {
                        iClientListenerToRemotePath.onRemote(gson.fromJson(json,ClickAction.class));
                    }
                }
            }
        });

    }




    /**
     * Admin Callback/Listener Interface
     */
    public interface IAdminListenerToDataPath{
        void onDataResponse(String data,@Nullable String message);
    }
    public interface IAdminListenerToRoomPath{
        void onRoom(Map<String,String> data,@Nullable String mesaage);
    }
    public interface IAdminPutRemoteCallback{
        void onPushSuccess();
        void onPushFailed(String error);
    }

    /**
     * Client Callback/Listener Interface
     */
    public interface IClientListenerToRemotePath{
        void onRemote(Action action);
    }
    public interface IClientCallbackToDataPath{
        void onSuccess();
        void onFailed(String error);
    }
    public interface IClientCallbackToRoomPath{
        void onSuccess();
        void onFailed(String error);
    }

}
