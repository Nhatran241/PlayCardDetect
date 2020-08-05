package com.machinelearning.playcarddetect.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.machinelearning.playcarddetect.R;
import com.machinelearning.playcarddetect.data.model.Card;
import com.machinelearning.playcarddetect.reciver.TakeBitmapOnTime;
import com.machinelearning.playcarddetect.server.ClientServerManager;
import com.machinelearning.playcarddetect.ui.admin.CardListAdapter;
import com.machinelearning.playcarddetect.ui.base.BaseActivity;

import java.util.List;

public class AdminActivity extends BaseActivity {
    private RecyclerView rv_first,rv_second,rv_third,rv_enemy;
    private CardListAdapter firstClientAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        initUi();
    }

    private void initUi() {
        rv_first = findViewById(R.id.rv_firstClient);
        rv_second = findViewById(R.id.rv_secondClient);
        rv_third = findViewById(R.id.rv_thirdClient);
        rv_enemy = findViewById(R.id.rv_enemy);

//        LinearLayoutManager layoutManager = new LinearLayoutManager/(this, LinearLayoutManager.HORIZONTAL, false);
        rv_first.setLayoutManager(new GridLayoutManager(this, 13));
//        rv_first.setLayoutManager(layoutManager);
    }

    @Override
    protected void PrepareServer() {
        showDialogLoading();
        ClientServerManager.getInstance().prepareClientServer(this, true, new ClientServerManager.IClientPrepareListener() {
            @Override
            public void OnPrepareClientServerSuccess() {
                dismisDialogLoading();
                ClientServerManager.getInstance().RegisterAdminListenerWithClients(new ClientServerManager.IAdminListener() {
                    @Override
                    public void OnClientFirstDataChange(List<Card> cardList) {
                        firstClientAdapter = new CardListAdapter(AdminActivity.this,cardList);
                        rv_first.setAdapter(firstClientAdapter);
                        for (int i = 0; i <cardList.size() ; i++) {
                            Log.d("nhatnhat", "OnClientFirstDataChange: "+cardList.get(i).getCardLevel());
                        }
                    }

                    @Override
                    public void OnClientSecondDataChange(List<Card> cardList) {

                    }

                    @Override
                    public void OnClientThirdDataChange(List<Card> cardList) {

                    }

                    @Override
                    public void OnClientTableCardDataChange(List<Card> cardList) {

                    }
                });
            }

            @Override
            public void OnPrepareClientServerFail(String error) {
                dismisDialogLoading();
                Toast.makeText(AdminActivity.this, ""+error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
