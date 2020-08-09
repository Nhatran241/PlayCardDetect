package com.machinelearning.playcarddetect.modules.client;

import androidx.annotation.Nullable;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.machinelearning.playcarddetect.common.Cons;
import com.machinelearning.playcarddetect.modules.datamanager.CaptureManager;
import com.machinelearning.playcarddetect.R;
import com.machinelearning.playcarddetect.modules.datamanager.ServerClientDataManager;
import com.machinelearning.playcarddetect.modules.client.service.ClientService;
import com.machinelearning.playcarddetect.common.BaseActivity;

public class ClientActivity extends BaseActivity{
    private static final int REQUESTCAPTURE = 1111;
    private static final int REQUESTACCESSIBILITY = 1233;
    private CaptureManager captureManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askPermission();
    }

    private void askPermission() {
        if(ClientService.isConnected) {
            requestCapture();
        }else {
            askAccessibilityPermission();
        }
    }

    private void askAccessibilityPermission() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivityForResult(intent,REQUESTACCESSIBILITY);
    }


    @Override
    protected void PrepareServer() {


    }

    private void requestCapture() {
        captureManager =CaptureManager.getInstance();
        captureManager.requestScreenshotPermission(this, REQUESTCAPTURE);
        captureManager.setOnGrantedPermissionListener(new CaptureManager.onGrantedPermissionListener() {
            @Override
            public void onResult(boolean isGranted) {
                if (isGranted) {
                    captureManager.init(ClientActivity.this);
                    Intent intent = new Intent(ClientActivity.this,ClientService.class);
                    intent.setAction(Cons.CAPTURE);
                    startService(intent);
                } else {
                    captureManager.requestScreenshotPermission(ClientActivity.this, REQUESTCAPTURE);
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode== REQUESTACCESSIBILITY) {
            if (ClientService.isConnected) {
                requestCapture();
            }else {
                Toast.makeText(this, "Accessibility Service not running . Try again or reset phone", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==REQUESTCAPTURE){
            captureManager.onActivityResult(resultCode, data);
        }
//            Intent intent = new Intent(this, ClientService.class);
//            List<CustomPath> pathList = new ArrayList<>();
//
//            CustomPath path1 = new CustomPath();
//            path1.moveTo(100,100);
//
//            CustomPath path2 = new CustomPath();
//            path2.moveTo(100,300);
//
//            CustomPath path3 = new CustomPath();
//            path3.moveTo(100,700);
//
//            CustomPath path4 = new CustomPath();
//            path4.moveTo(100,900);
//            pathList.add(path1);
//            pathList.add(path2);
//            pathList.add(path3);
//            pathList.add(path4);
//
//            RemoteProfile remoteProfile = new RemoteProfile(RemoteProfile.RemoteType.CLICK,pathList,0,3000,3000,500);
//            intent.putExtra(Cons.REMOTEPROFILE, remoteProfile);
//            startService(intent);
    }


}
