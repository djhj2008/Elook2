package com.elook.client;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.elook.client.location.LocationWrapper;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.user.AdvertInfo;
import com.elook.client.user.UserInfo;

import java.io.File;
import java.util.List;

/**
 * Created by xy on 7/21/16.
 */
public class StartService extends Service{
    private static final String TAG = "StartService";
    ELookApplication myapp;
    LocationWrapper mLocationWrapper;
    Thread t;
    QueryLocationTask qltask;

    public void MyMethod(){
        Log.i(TAG, "BindService-->MyMethod()");
        qltask = new QueryLocationTask();
        qltask.execute();
        //doujun temp modify for tiger
        //t = new Thread(mLoginRunable);
        //t.start();

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate");
        super.onCreate();
        myapp = (ELookApplication)getApplication();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        MyMethod();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        mLocationWrapper=null;
        if(qltask!=null&&!qltask.isCancelled()){
            qltask.cancel(true);
        }
        if(t!=null&&t.isAlive()){
            t.interrupt();
        }
        qltask=null;
        t=null;
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public String mLocationString = null;
    class QueryLocationTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            mLocationWrapper = new LocationWrapper(StartService.this);
            mLocationWrapper.queryCurrentLocation(mLocationListener);
            return mLocationString;
        }

        @Override
        protected void onPostExecute(String address) {

        }
    }

    LocationWrapper.OnLocationChangedListener mLocationListener = new LocationWrapper.OnLocationChangedListener() {
        @Override
        public void onLocationChanged(String province, String city, String township, String street, String streetNumber , String building,double latitude,double longitude) {
            if(mLocationString == null && building != null){
                mLocationString = building;
                myapp.setLocation(mLocationString);
                myapp.setLatitude(latitude);
                myapp.setLongitude(longitude);
            }
            t = new Thread(mLoginRunable);
            t.start();
        }
    };

    private void downloadAdvertPic(){
        ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(StartService.this);
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
                ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(StartService.this);
                UserInfo info = databaseHelper.getActiveUserInfo();
                myapp.setUserInfo(info);
                downloadAdvertPic();
            }
            Intent intent = new Intent("com.example.communication.RECEIVER");
            intent.putExtra("login", isSuccessfully);
            sendBroadcast(intent);
        }
    };

}
