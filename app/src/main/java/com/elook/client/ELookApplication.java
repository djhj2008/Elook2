package com.elook.client;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookService;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.UserInfo;
import com.elook.client.utils.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 5/19/16.
 */
public class ELookApplication extends Application {
    private static final String TAG = "ELookApplication";

    public static final String ELOOK_SERVICE_ACTION = "com.elook.client.service.ELookService";
    ELookServiceInterface mService;
    static Context mContext;
    private List<Activity> mAllActivities = new ArrayList<>();

    UserInfo mUserInfo=null;
    String mLocation=null;
    double mLatitude;
    double mLongitude;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        Intent intent = new Intent(ELOOK_SERVICE_ACTION);
        intent.setPackage(getPackageName());
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        mContext = getBaseContext();
    }

    public void setUserInfo(UserInfo info){
        mUserInfo = info;
        Log.d(TAG,"ELookApplication mUserInfo");
        Log.d(TAG,"ELookApplication name:"+info.getUserPhoneName());
        Log.d(TAG,"ELookApplication logind:"+info.getUserLoginStatus());
        Log.d(TAG,"ELookApplication last_logind:"+info.getIsLastLogin());

    }

    public void setLocation(String mLocation) {
        this.mLocation = mLocation;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public UserInfo getUserInfo(){
        return mUserInfo;
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d("APPLication", "onServiceConnected");
            mService = ELookServiceInterface.Stub.asInterface(service);
            ELServiceHelper helper = new ELServiceHelper(mService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };



    public void addActivity(Activity activity){
        mAllActivities.add(activity);
    }

    public void removeActivity(Activity activity){
        if(mAllActivities.contains(activity))mAllActivities.remove(activity);
    }
    public void removeMainActivity(){
        for(Activity activity:mAllActivities){
            activity.finish();
        }
    }

}
