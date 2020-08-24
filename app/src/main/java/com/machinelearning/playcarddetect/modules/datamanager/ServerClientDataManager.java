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
import com.machinelearning.playcarddetect.common.model.CardBase64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerClientDataManager {
    private static ServerClientDataManager instance;
    public static String RESPONSE_SUCCESS = "success";
    public static String RESPONSE_FAIL = "failed";
    private FirebaseFirestore db ;
    private String deviceId;
    private List<String> listDevice = new ArrayList<>();

    public static ServerClientDataManager getInstance() {
        if(instance==null)
            instance = new ServerClientDataManager();

        return instance;
    }
    @SuppressLint("HardwareIds")
    public void prepareClientServer(Context context,boolean isAdmin, IClientPrepareListener iPrepareClientServerListener){
       db = FirebaseFirestore.getInstance();
       deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
       if(isAdmin){
        RegisterAdminToServer("Devices",iPrepareClientServerListener);
       }else
        RegisterClientToServer(deviceId, "Devices", iPrepareClientServerListener);
    }
    private void RegisterClientToServer(String id,String collectionPath,IClientPrepareListener iPrepareClientServerListener){

        Map<String, String> connection = new HashMap<>();
        connection.put("time", System.currentTimeMillis()+"");
        db.collection(collectionPath)
                .document(id)
                .set(connection).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                db.collection("Click").document(id).set(connection);
                 iPrepareClientServerListener.OnPrepareClientServerSuccess();;

            }
        })
               .addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       iPrepareClientServerListener.OnPrepareClientServerFail("Fail to Register"+e.toString());

                   }
               });
    }
    private void RegisterAdminToServer(String collectionPath,IClientPrepareListener iClientPrepareListener){
       db.collection(collectionPath).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot document = task.getResult();
                    for (int i = 0; i <document.getDocuments().size() ; i++) {
                        listDevice.add(document.getDocuments().get(i).getId());
                    }

                    iClientPrepareListener.OnPrepareClientServerSuccess();
                } else {
                    Log.d("nhatnhat", "get failed with ", task.getException());
                    iClientPrepareListener.OnPrepareClientServerFail(task.getException()+"");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iClientPrepareListener.OnPrepareClientServerFail(e.toString());
            }
        });


    }

    /**
     * Admin function zone
     */
    public void RegisterAdminToServer(IAdminWaitingClientsJoinRoom iAdminListener){
        /**
         * Register Admin đến Room
         * Room [
         *   RoomID [ max 4
         *     DeviceID1
         *     DeviceID2
         *     DeviceID3
         *     DeviceID4
         *   ]
         * ]
         *
         *  Khi RoomID.size == 4 Có nghĩa đã đủ Client join vào phòng sẽ bắt đầu yêu cầu Client gửi dữ liệu
         */
        String roomPath = "Room";
        if(db==null)
            db=FirebaseFirestore.getInstance();
        db.collection(roomPath).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value!=null) {
                    List<DocumentChange> documentChangeList = value.getDocumentChanges();
                    for (DocumentChange document:documentChangeList) {
                        Map<String, Object> map = document.getDocument().getData();
                        if(map.size()==4){
                            List<String> list = new ArrayList<>();
                            for (Map.Entry<String, Object> entry : map.entrySet()) {
                                list.add(entry.getValue().toString());
                            }
                            iAdminListener.onClientJoinSuccess(document.toString(),list);
                        }
                    }
                }

            }
        });


//        if(listDevice.size()>0){
//            for (int i = 0; i <listDevice.size() ; i++) {
//                int finalI = i;
//                db.collection("Room").document(listDevice.get(i)).addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
//                        List<CardBase64> cards = new ArrayList<>();
//                        Map<String,Object> map = documentSnapshot.getData();
//                        for (int j = 0; j <map.size() ; j++) {
//                            CardBase64 card =documentSnapshot.get(j+"",CardBase64.class);
//                            if(card!=null) {
//                                cards.add(card);
//                            }
//                        }
//                        if(finalI ==0){
//                            iAdminListener.OnClientFirstDataChange(cards,listDevice.get(finalI));
//                        }
//                    }
//                });
//            }
//        }
    }
    public void RequestClientData(String clientId,IRequestClientData iRequestClientData){
        /**
         * Admin listener đến DeviceID trong Path DEVICES
         * khi client đẩy dữ liệu lên server admin sẽ nhận dữ liệu
         */
        String path = "Devices";
        if(db==null)
            db=FirebaseFirestore.getInstance();
        db.collection(path).document(clientId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            List<CardBase64> cards = new ArrayList<>();
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value!=null){
                        Map<String,Object> map = value.getData();
                        if(map!=null){
                            for (int j = 0; j <map.size() ; j++) {
                                CardBase64 card =value.get(j+"",CardBase64.class);
                                if(card!=null) {
                                    cards.add(card);
                                }
                            }
                            iRequestClientData.onClientDataResponse(cards,RESPONSE_SUCCESS);
                        }else {
                            iRequestClientData.onClientDataResponse(cards,RESPONSE_FAIL);
                        }
                }else {
                    iRequestClientData.onClientDataResponse(cards,RESPONSE_FAIL);
                }
            }
        });
    }

    /**
     * Client function zone
     */
    public void RegisterClientToServer(){

    }
    public void RegisterClientToRoom(Context context,int room){
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String roomPath = "Room";
        if(db==null)
            db=FirebaseFirestore.getInstance();
        db.collection(roomPath).document(room+"").set(deviceId).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }


    public void RegisterClientListenerWithServer(IClientListener iClientListener){
        db.collection("Click").document(deviceId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.getData()!=null) {
                    if(documentSnapshot.get("cardclick")!=null)
                        iClientListener.OnServerClickCard(Integer.parseInt(String.valueOf(documentSnapshot.get("cardclick"))));




                }

            }
        });


    }

    public void putClientHandCards(List<CardBase64> listCardsInHand,IClientPutValueListener iClientPutValueListener) {
        Map<String, CardBase64> cards = new HashMap<>();
        for (int i = 0; i <listCardsInHand.size() ; i++) {
            cards.put(i+"",listCardsInHand.get(i));
        }
        db.collection("Devices")
                .document(deviceId).set(cards).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                iClientPutValueListener.OnClientPutValueSuccess();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iClientPutValueListener.OnClientPutValueFail(e.toString());
            }
        });
    }

    public void putRemote(String clientID, int position) {
        Map<String,Integer> click = new HashMap<>();
        click.put("cardclick",position);
        db.collection("Click")
                .document(clientID).set(click).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    public interface IClientPrepareListener{
      void OnPrepareClientServerSuccess();
       void OnPrepareClientServerFail(String error);
    }
    public interface IClientPutValueListener{
        void OnClientPutValueSuccess();
        void OnClientPutValueFail(String error);
    }
    public interface IClientListener{
        void OnServerClickCard(int position);
        void OnServerClickXepBai();
    }
    public interface IAdminWaitingClientsJoinRoom{
        void onClientJoinSuccess(String roomID,List<String> listDeviceID);
    }
    public interface IRequestClientData{
        void onClientDataResponse(List<CardBase64> cardBase64List,String response);
    }
}
