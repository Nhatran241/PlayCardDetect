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
    public void RegisterAdminListenerWithClients(IAdminListener iAdminListener){
        if(listDevice.size()>0){
            for (int i = 0; i <listDevice.size() ; i++) {
                int finalI = i;
                db.collection("Devices").document(listDevice.get(i)).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        List<CardBase64> cards = new ArrayList<>();
                        Map<String,Object> map = documentSnapshot.getData();
                        for (int j = 0; j <map.size() ; j++) {
                            CardBase64 card =documentSnapshot.get(j+"",CardBase64.class);
                            if(card!=null) {
                                cards.add(card);
                            }
                        }
                        if(finalI ==0){
                            iAdminListener.OnClientFirstDataChange(cards,listDevice.get(finalI));
                        }
                    }
                });
            }
        }
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

    private void seletectDevice(String collectionPath,List<String> listDevice) {
        db.collection(collectionPath).document(listDevice.get(0)).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

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
    public interface IAdminListener{
        void OnClientFirstDataChange(List<CardBase64> cardList,String id);
        void OnClientSecondDataChange(List<CardBase64> cardList);
        void OnClientThirdDataChange(List<CardBase64> cardList);
        void OnClientTableCardDataChange(List<CardBase64> cardList);
    }
}
