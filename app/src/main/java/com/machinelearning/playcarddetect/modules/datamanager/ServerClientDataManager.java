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
import com.machinelearning.playcarddetect.modules.accessibilityaction.Cons;
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.Action;
import com.machinelearning.playcarddetect.common.model.CardBase64;
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.ActionResponse;
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.ClickAction;
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.GestureAction;
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.MultipleGestureAction;
import com.machinelearning.playcarddetect.modules.accessibilityaction.action.SwipeAction;
import com.machinelearning.playcarddetect.modules.client.DeviceStats;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServerClientDataManager {
    private static ServerClientDataManager instance;
    public static String RESPONSE_SUCCESS = "success";
    public static String RESPONSE_FAIL = "failed";
    String remotePath ="Remote";
    private String remotePath_actionResponse ="actionResponse";;
    private String remotePath_actionType="actionType";
    private String devicesPath_deviceStats="deviceStats";
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
    public void AdminListenerToDeviceStatsPath(IAdminListenerToDeviceStatsPath iAdminListenerToDeviceStatsPath){
        db.collection(devicesPath).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Map<String,String> map = new HashMap<>();
                for (DocumentSnapshot a:queryDocumentSnapshots.getDocuments()) {
                    map.put(a.getId(),String.valueOf(a.get(devicesPath_deviceStats)));
                }
                iAdminListenerToDeviceStatsPath.onDeviceStatsReponse(map,e+"");
            }
        });
    }
    public void AdminPushRemote(Action action, String deviceIdListenerAction, IAdminPutRemoteCallback iAdminPutRemoteCallback){
//        Map<String,Action> map = new HashMap<>();
        db.collection(remotePath).document(deviceIdListenerAction).set(action).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection(remotePath).document(deviceIdListenerAction).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value.get(remotePath_actionResponse)!=null){
                            ActionResponse actionResponse = ActionResponse.valueOf(Objects.requireNonNull(value.get(remotePath_actionResponse)).toString());
                            if(actionResponse!=ActionResponse.WAITING){
                                iAdminPutRemoteCallback.onAdminPutRemoteResponse(actionResponse,deviceIdListenerAction);
                            }
                        }else {
                            iAdminPutRemoteCallback.onAdminPutRemoteResponse(ActionResponse.FAILED,deviceIdListenerAction);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAdminPutRemoteCallback.onAdminPutRemoteResponse(ActionResponse.FAILED,deviceIdListenerAction);
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
    public void ClientPushDeviceStats(DeviceStats deviceStats, IClientCallbackToRoomPath iClientCallbackToRoomPath){
        final Map<String, DeviceStats> deviceStatsMap = new HashMap<>();
        deviceStatsMap.put(devicesPath_deviceStats,deviceStats);
        db.collection(devicesPath).document(deviceId).set(deviceStatsMap,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
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
        db.collection(remotePath).document(deviceId).addSnapshotListener((documentSnapshot, e) -> {
            assert documentSnapshot != null;
            Action actions = ServerClientDataManagerExtKt.mappingActions(documentSnapshot,documentSnapshot.get(remotePath_actionType).toString());
            iClientListenerToRemotePath.onRemote(actions);
        });

    }

    public void ClientPushRemoteResponse(@NotNull ActionResponse it) {
        Map<String,ActionResponse> map =new HashMap<>();
        map.put(remotePath_actionResponse,it);
        db.collection(remotePath).document(deviceId).set(map,SetOptions.merge());
    }


    /**
     * Admin Callback/Listener Interface
     */
    public interface IAdminListenerToDataPath{
        void onDataResponse(String data,@Nullable String message);
    }
    public interface IAdminListenerToDeviceStatsPath{
        void onDeviceStatsReponse(Map<String,String> data,@Nullable String mesaage);
    }
    public interface IAdminPutRemoteCallback{
        void onAdminPutRemoteResponse(ActionResponse actionResponse,String deviceId);
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
