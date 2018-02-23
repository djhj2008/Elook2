package com.elook.client.el.initialize;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.util.Log;

import com.elook.client.service.ELServiceHelper;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.user.AccessPointInfo;
import com.elook.client.wifi.WiFiAdmin;
import com.elook.client.wifi.WiFiListPreview;

/**
 * Created by haiming on 5/9/16.
 */
public class ReconnectToWiFiState extends BaseState {
    private static final String TAG = "ConnectToWiFiState";

    private Context mContext;
    private WiFiAdmin mWiFiAdmin;

    public ReconnectToWiFiState(Context context) {
        super(context);
        this.mContext = context;
        this.mWiFiAdmin = new WiFiAdmin(context);
        mResultCode = RESULT_READY_TO_CONNECT_WIFI;
    }


    @Override
    public void start() {
        mWiFiAdmin.collectApInfos();
        if(mCallback != null) mCallback.onStart();
        ELookDatabaseHelper databaseHelper = ELookDatabaseHelper.newInstance(mContext);
        AccessPointInfo accessPointInfo = databaseHelper.getActivedApInfo();
        String passwd = databaseHelper.getPasswordOfAp(accessPointInfo);
        if(accessPointInfo != null && passwd != null){
            if(connectToWiFi(accessPointInfo, passwd)){
                mResultCode = RESULT_CONNECTED_WIFI_OK;
            } else {
                mResultCode = RESULT_CONNECTED_WIFI_ERROR;
            }
        }
        if(mCallback != null) mCallback.onFinished();

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
}
