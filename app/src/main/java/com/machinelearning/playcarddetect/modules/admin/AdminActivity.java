package com.machinelearning.playcarddetect.modules.admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.machinelearning.playcarddetect.R;
import com.machinelearning.playcarddetect.common.model.Card;
import com.machinelearning.playcarddetect.common.model.CardBase64;
import com.machinelearning.playcarddetect.modules.admin.adapter.CardListAdapter;
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager;
import com.machinelearning.playcarddetect.common.BaseActivity;

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
        ServerClientDataManager.getInstance().prepareClientServer(this, true, new ServerClientDataManager.IClientPrepareListener() {
            @Override
            public void OnPrepareClientServerSuccess() {
                dismisDialogLoading();
                ServerClientDataManager.getInstance().RegisterAdminListenerWithClients(new ServerClientDataManager.IAdminListener() {
                    @Override
                    public void OnClientFirstDataChange(List<CardBase64> cardList, String id) {
                        firstClientAdapter = new CardListAdapter(AdminActivity.this,cardList,id);
                        rv_first.setAdapter(firstClientAdapter);
                    }

                    @Override
                    public void OnClientSecondDataChange(List<CardBase64> cardList) {

                    }

                    @Override
                    public void OnClientThirdDataChange(List<CardBase64> cardList) {

                    }

                    @Override
                    public void OnClientTableCardDataChange(List<CardBase64> cardList) {

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
