package com.machinelearning.playcarddetect.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.machinelearning.playcarddetect.R;
import com.machinelearning.playcarddetect.ui.base.BaseActivity;

public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);
    }

    public void onAdmin(View view) {
        startActivity(new Intent(this,AdminActivity.class));
    }

    public void onClient(View view) {
        startActivity(new Intent(this,ClientActivity.class));
    }
}