package com.machinelearning.playcarddetect;

import android.app.Application;

import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ServerClientDataManager.getInstance().init(this);
    }
}
