package com.machinelearning.playcarddetect;

import android.app.Application;
import android.util.Log;

import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ServerClientDataManager.getInstance().init(this);

//        try {
//            Process p = Runtime.getRuntime().exec(new String[]{"wm","size","480x720"});
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
