package com.elook.client;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elook.client.activity.LoginActivity;
import com.elook.client.activity.MainContentActivity;
import com.elook.client.activity.RegisteActivity;
import com.elook.client.location.LocationWrapper;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.user.AdvertInfo;
import com.elook.client.user.UserInfo;
import com.elook.client.utils.ELUtils;

import java.io.File;
import java.util.List;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final boolean DEBUG = false;

    ELookApplication myapp;
    LocationWrapper mLocationWrapper;
    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        initViews();
        myapp = (ELookApplication)getApplication();
    }

    private void initViews() {
        //setContentView(R.layout.activity_main);
    }

    private void downloadAdvertPic(){
        ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(MainActivity.this);
        List<AdvertInfo> infos = databaseHelper.getAdvertInfo();
        int banner_count = infos.size();
        for(int i=0;i<banner_count;i++){
            String picurl=infos.get(i).getAdvertPicUrl();
            if(picurl!=null){
                if(!picurl.startsWith("http://"))
                    continue;
                int index =picurl.lastIndexOf("/");
                if(index > 0) {
                    String newPicUrl = picurl.substring(index+1);
                    Log.d(TAG,"downloadAdvertPic name:"+newPicUrl);
                    final String targetLocalPath = getFilesDir().getAbsolutePath() +
                            "/" + "ADVERT" + "/" + newPicUrl;
                    File imageFile = new File(targetLocalPath);
                    if(imageFile.exists()){
                        continue;
                    }
                    File parentFile = imageFile.getParentFile();
                    if (!parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    if(!imageFile.exists()){
                        ELServiceHelper instanceHelper = ELServiceHelper.get();
                        instanceHelper.loadImageFromeServer(picurl, targetLocalPath);
                    }
                }
            }
        }
    }

    Runnable mLoginRunable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "mLoginRunable start");
            ELServiceHelper serviceHelper = ELServiceHelper.get();
            Log.d(TAG, "mLoginRunable get");
            boolean isSuccessfully = serviceHelper.loginWithActivedUser();
            if (isSuccessfully) {
                ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(MainActivity.this);
                UserInfo info = databaseHelper.getActiveUserInfo();
                myapp.setUserInfo(info);
                downloadAdvertPic();
                Intent intent = new Intent(MainActivity.this, MainContentActivity.class);
                intent .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }else{
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        t = new Thread(mLoginRunable);
        t.start();
        Log.d(TAG, "onStart");

    }


    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        mLocationWrapper=null;

        if(t!=null&&t.isAlive()){
            t.interrupt();
        }
        t=null;
        //setContentView(R.layout.activity_null);
        super.onDestroy();
    }
}
