package com.elook.client.service;

import android.app.Service;
import android.content.Intent;

import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.elook.client.ELookServiceInterface;

/**
 * Created by haiming on 3/27/16.
 */
public class ELookService extends Service {
    private static final String TAG = "ELookService";

    ELookServiceInterface.Stub mService;
    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        mService = new ELookServiceImpl(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mService;
    }
}
