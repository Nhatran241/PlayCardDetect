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
import com.machinelearning.playcarddetect.modules.client.DeviceState;
import com.machinelearning.playcarddetect.modules.client.DeviceStateBundle;

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
    private ArrayList<String> listDevicesAlreadyListener = new ArrayList<>();

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
                Map<String,DeviceStateBundle> map = new HashMap<>();
                for (DocumentSnapshot a:queryDocumentSnapshots.getDocuments()) {
                    map.put(a.getId(),a.toObject(DeviceStateBundle.class));
                }
                iAdminListenerToDeviceStatsPath.onDeviceStatsReponse(map,e+"");
            }
        });
    }
    public void AdminListenerToRemotePath(String deviceIdListenerAction,IAdminPutRemoteCallback iAdminPutRemoteCallback){
        if(listDevicesAlreadyListener.contains(deviceIdListenerAction))
            return; //Admin already listener to this device
        db.collection(remotePath).document(deviceIdListenerAction).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        listDevicesAlreadyListener.add(deviceIdListenerAction);
                        if(value.get(remotePath_actionResponse)!=null&&value.get(remotePath_actionType)!=null){
                            ActionResponse actionResponse = ActionResponse.valueOf(Objects.requireNonNull(value.get(remotePath_actionResponse)).toString());
                            if(actionResponse!=ActionResponse.WAITING){
                                iAdminPutRemoteCallback.onAdminPutRemoteResponse(actionResponse,value.get(remotePath_actionType).toString(),deviceIdListenerAction);
                            }
                        }else {
                            iAdminPutRemoteCallback.onAdminPutRemoteResponse(ActionResponse.FAILED,value.get(remotePath_actionType).toString(),deviceIdListenerAction);
                        }
                    }
                });
    }
    public void AdminPushRemote(Action action, String deviceIdListenerAction, IAdminPutRemoteCallback iAdminPutRemoteCallback){
//        Map<String,Action> map = new HashMap<>();
        Log.d("admin_push_aciton", "AdminPushRemote: "+action.actionType);
        if(action.actionType.equals(Cons.EmptyActionType))
            return;
        db.collection(remotePath).document(deviceIdListenerAction).set(action).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                iAdminPutRemoteCallback.onAdminPutRemoteResponse(ActionResponse.WAITING,action.actionType,deviceIdListenerAction);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iAdminPutRemoteCallback.onAdminPutRemoteResponse(ActionResponse.FAILED,action.actionType,deviceIdListenerAction);
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
    public void ClientPushDeviceStats(DeviceStateBundle deviceStateBundle, IClientPushDeviceStatsCallback iClientPushDeviceStatsCallback){
//        final Map<String, DeviceStateBundle> deviceStatsMap = new HashMap<>();
//        deviceStatsMap.put(devicesPath_deviceStats, deviceStateBundle);
        db.collection(devicesPath).document(deviceId).set(deviceStateBundle,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                iClientPushDeviceStatsCallback.onResponse(ActionResponse.COMPLETED, deviceStateBundle);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iClientPushDeviceStatsCallback.onResponse(ActionResponse.FAILED, deviceStateBundle);
            }
        });
    }
    public void ClientListenerToRemotePath(IClientListenerToRemotePath iClientListenerToRemotePath){
        db.collection(remotePath).document(deviceId).addSnapshotListener((documentSnapshot, e) -> {
            assert documentSnapshot != null;
            Log.d("clientListenerToRemotePath", "ClientListenerToRemotePath: "+documentSnapshot.get(remotePath_actionResponse)+"/"+ActionResponse.WAITING);
            if(documentSnapshot.get(remotePath_actionResponse).toString().equals(ActionResponse.WAITING.name())) {
                Action actions = ServerClientDataManagerExtKt.mappingActions(documentSnapshot, documentSnapshot.get(remotePath_actionType).toString());
                iClientListenerToRemotePath.onRemote(actions);
            }
        });

    }

    public void ClientPushRemoteResponse(@NotNull ActionResponse it,IClientPushRemoteResponseCallback iClientPushRemoteResponseCallback) {
        Map<String,ActionResponse> map =new HashMap<>();
        map.put(remotePath_actionResponse,it);
        db.collection(remotePath).document(deviceId).set(map,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                iClientPushRemoteResponseCallback.onResponse(ActionResponse.COMPLETED);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iClientPushRemoteResponseCallback.onResponse(ActionResponse.FAILED);
            }
        });
    }


    /**
     * Admin Callback/Listener Interface
     */
    public interface IAdminListenerToDataPath{
        void onDataResponse(String data,@Nullable String message);
    }
    public interface IAdminListenerToDeviceStatsPath{
        void onDeviceStatsReponse(Map<String,DeviceStateBundle> data,@Nullable String mesaage);
    }
    public interface IAdminPutRemoteCallback{
        void onAdminPutRemoteResponse(ActionResponse actionResponse,String actionType,String deviceId);
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
    public interface IClientPushDeviceStatsCallback{
        void onResponse(ActionResponse actionResponse, DeviceStateBundle deviceStateBundle);
    }
    public interface IClientPushRemoteResponseCallback{
        void onResponse(ActionResponse actionResponse);
    }

}
