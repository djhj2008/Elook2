package com.elook.client;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.elook.client.activity.LoginActivity;
import com.elook.client.activity.MainContentActivity;


public class StartActivity extends Activity {
    private static final String TAG = "StartActivity ";
    private boolean flag = false;
    private MsgReceiver msgReceiver;
    private static int LOCATION_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        //动态注册广播接收器
        msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.communication.RECEIVER");
        registerReceiver(msgReceiver, intentFilter);
    }

    public static int getAndroidSDKVersion() {
        int version = 0;
        try {
            version = Integer.valueOf(android.os.Build.VERSION.SDK);
        } catch (NumberFormatException e) {

        }
        return version;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        if(getAndroidSDKVersion()>=23){
            Log.d(TAG,"AOS6.0");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //申请LOCATION权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_REQUEST_CODE);
            }else{
                mHandler.sendEmptyMessageDelayed(MSG_READY_TO_START_MAIN, 100);
            }
        } else{
            mHandler.sendEmptyMessageDelayed(MSG_READY_TO_START_MAIN, 100);
        }
    }


    private static final int MSG_READY_TO_START_MAIN = 0;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mHandler.removeMessages(MSG_READY_TO_START_MAIN);
            int what = msg.what;
            switch (what) {
                case MSG_READY_TO_START_MAIN:
                    Intent intent = new Intent(StartActivity.this, StartService.class);
                    startService(intent);
                    break;
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode,grantResults);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mHandler.sendEmptyMessageDelayed(MSG_READY_TO_START_MAIN,100);
                // Permission Granted
            } else {
                // Permission Denied
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        Intent iService=new Intent(StartActivity.this,StartService.class);
        iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        stopService(iService);
        unregisterReceiver(msgReceiver);
        super.onDestroy();
    }

    public class MsgReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //拿到进度，更新UI
            boolean flag = intent.getBooleanExtra("login",false);
            if (flag) {
                Intent it = new Intent(StartActivity.this, MainContentActivity.class);
                it .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
            }else{
                Intent it = new Intent(StartActivity.this, LoginActivity.class);
                it .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(it);
            }
            finish();
        }

    }

}
