package com.machinelearning.playcarddetect.modules.datamanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.machinelearning.playcarddetect.common.Action;
import com.machinelearning.playcarddetect.common.model.CardBase64;
import com.machinelearning.playcarddetect.common.model.ClientModel;

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
    private FirebaseFirestore db ;
    private String deviceId;
    private List<String> listDevice = new ArrayList<>();

    public static ServerClientDataManager getInstance() {
        if(instance==null)
            instance = new ServerClientDataManager();

        return instance;
    }


    public void init(Context context){
        if(db == null)
            db = FirebaseFirestore.getInstance();
    }
    /**
     * Admin function
     */

    public void AdminListenerToDataPath(IAdminListenerToDataPath iAdminListenerToDataPath){
        db.collection(dataPath).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                iAdminListenerToDataPath.onDataResponse("Admin_listener_data",e+"");
            }
        });
    }
    public void AdminListenerToRoomPath(IAdminListenerToRoomPath iAdminListenerToRoomPath){
        db.collection(roomPath).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                assert e != null;
                iAdminListenerToRoomPath.onRoom("Admin_listener_room",e+"");
            }
        });
    }
    public void AdminPushRemote(Action action,String deviceIdListenerAction,IAdminPutRemoteCallback iAdminPutRemoteCallback){
        db.collection(remotePath).document(deviceIdListenerAction).set(action).addOnSuccessListener(new OnSuccessListener<Void>() {
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
    public void ClientPushRoom(String room,String deviceId,IClientCallbackToRoomPath iClientCallbackToRoomPath){
        db.collection(roomPath).document(deviceId).set(room).addOnSuccessListener(new OnSuccessListener<Void>() {
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
    public void ClientListenerToRemotePath(String deviceId,IClientListenerToRemotePath iClientListenerToRemotePath){
        db.collection(remotePath).document(deviceId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                iClientListenerToRemotePath.onRemote("Client_listener_remote_oke");
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
        void onRoom(String data,@Nullable String mesaage);
    }
    public interface IAdminPutRemoteCallback{
        void onPushSuccess();
        void onPushFailed(String error);
    }

    /**
     * Client Callback/Listener Interface
     */
    public interface IClientListenerToRemotePath{
        void onRemote(String action);
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
