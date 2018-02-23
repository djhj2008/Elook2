package com.elook.client.el.initialize;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.elook.client.service.ELServiceHelper;
import com.elook.client.user.AccessPointInfo;
import com.elook.client.wifi.WiFiAdmin;
import com.elook.client.wifi.WiFiListPreview;

/**
 * Created by haiming on 5/5/16.
 */
public class ConnectToWiFiState extends BaseState  implements WiFiListPreview.OnWiFiSelected{
    private static final String TAG = "ConnectToWiFiState";

    private Context mContext;
    WiFiListPreview mWifiListPreview;
    private WiFiAdmin mWiFiAdmin;

    public ConnectToWiFiState(Context context){
        super(context);
        this.mContext = context;
        this.mWiFiAdmin = new WiFiAdmin(context);
        mResultCode = RESULT_READY_TO_CONNECT_WIFI;
    }

    @Override
    public void start() {
        mWifiListPreview = new WiFiListPreview(mContext, mWiFiAdmin, this);
        mWiFiAdmin.registeScanFinishedListener(mWifiListPreview);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mWiFiAdmin.collectApInfos();
            }
        }).start();
        mWifiListPreview.show();
        if(mCallback != null) mCallback.onStart();
    }

    @Override
    public void stopGetThread() {

    }

    private boolean connectToWiFi(AccessPointInfo info, String passwd) {
        boolean isConnectedSuccessful = false;
        try {
            isConnectedSuccessful = mWiFiAdmin.connectToAp(info, passwd);
        } catch (NetworkErrorException e) {
            e.printStackTrace();
            isConnectedSuccessful = false;
        }
        if (isConnectedSuccessful) {
            ELServiceHelper helper = ELServiceHelper.get();
            helper.saveWifiInfomartion(info, passwd);
        }
        return isConnectedSuccessful;
    }

    @Override
    public void onWiFiSelected(final AccessPointInfo info, final String passwd) {
        if(mWifiListPreview.isShowing())mWifiListPreview.dismiss();
        mResultCode = RESULT_CONNECTING_WIFI;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(connectToWiFi(info, passwd)){
                    mResultCode = RESULT_CONNECTED_WIFI_OK;
                } else {
                    mResultCode = RESULT_CONNECTED_WIFI_ERROR;
                }
                if(mCallback != null) mCallback.onFinished();
            }
        }).start();

    }
}
