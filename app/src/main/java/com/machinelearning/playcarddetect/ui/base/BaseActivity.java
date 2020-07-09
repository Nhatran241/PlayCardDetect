package com.machinelearning.playcarddetect.ui.base;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    protected abstract void PrepareServer();
    private ProgressDialog dialog;


    public void showDialogLoading(){
        dialog = ProgressDialog.show(this, "",
                "Connect Client To Server", true);
    }
    public void dismisDialogLoading(){
        if(dialog!=null) {
            if(dialog.isShowing()) {
                dialog.cancel();
            }
            dialog=null;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PrepareServer();
    }
}
