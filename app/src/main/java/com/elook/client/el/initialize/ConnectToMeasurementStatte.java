package com.elook.client.el.initialize;

import android.accounts.NetworkErrorException;
import android.app.AlertDialog;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.elook.client.R;
import com.elook.client.service.ELookDatabaseHelper;
import com.elook.client.service.ReceivedPacket;
import com.elook.client.service.SendPacket;
import com.elook.client.user.AccessPointInfo;
import com.elook.client.wifi.DeviceClient;
import com.elook.client.wifi.WiFiAdmin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 5/5/16.
 */
public class ConnectToMeasurementStatte extends BaseState implements WiFiAdmin.OnCollectAPInfoFinished {
    private static final String TAG = "ConnectToMeasurement";
    private WiFiAdmin mWiFiAdmin;
    DeviceClient mDeviceClient;
    String mSharedWiFiSSID = "";
    String mSharedWiFiPWD = "";
    String mELKSSID = "";
    int mDeviceId;
    private Context mContext;
    ELookDatabaseHelper mDatabaseHelper;
    List<AccessPointInfo> mAllAPInfos = new ArrayList<AccessPointInfo>();

    public ConnectToMeasurementStatte(Context context, int devId) {
        super(context);
        mContext = context;
        this.mWiFiAdmin = new WiFiAdmin(context);
        mDeviceClient = new DeviceClient(context);
        mDatabaseHelper = ELookDatabaseHelper.newInstance(context);
        this.mDeviceId = devId;
        this.mELKSSID = "elk" + mDeviceId;
        mWiFiAdmin.registeScanFinishedListener(this);
        mWiFiAdmin.collectApInfos();
        initWiFiInfo();
        mResultCode = RESULT_READY_TO_CONNECT_DEV;
    }

    private void initWiFiInfo() {
        AccessPointInfo activedInfo = mDatabaseHelper.getActivedApInfo();
        this.mSharedWiFiSSID = activedInfo.getName();
        this.mSharedWiFiPWD = mDatabaseHelper.getPasswordOfAp(activedInfo);
    }
    Thread t;
    @Override
    public void start() {
        if (mCallback != null) mCallback.onStart();
        t = new Thread(new Runnable() {
        @Override
        public void run() {
        boolean isScannedELK = false;
        boolean isConnectedELKSuccessful = false;
        mAllAPInfos = mWiFiAdmin.getCollectedApInfos();
        if (!mAllAPInfos.isEmpty()) {
            if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_SCAN);//connect fail.
            isScannedELK = hasScannedELK();
        }
        if (isScannedELK) {
            if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_SCAN_OK);//connect fail.
            if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_CONNECT);//connect fail.
            isConnectedELKSuccessful = connectToEasyLink(mELKSSID);
            if (isConnectedELKSuccessful) {
                if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_CONNECT_OK);//connect fail.
                boolean isELKConnected = isELKConnected();
                //new Thread(new Runnable() {
                    //@Override
                    //public void run() {
                        shareWiFiInfoToDevice();
                   // }
               // }).start();

            }else {
                if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_CONNECT_FAIL);//connect fail.
            }
        }else{
            if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_SCAN_FAIL);//scan fail.
        }
             }
            });
        t.start();
    }

    @Override
    public void stopGetThread() {
        if (t!=null&&t.isAlive()) {
            t.interrupt();
            t = null;
        }
    }

    @Override
    public void onCollectAPInfoFinished() {
        this.mAllAPInfos = mWiFiAdmin.getCollectedApInfos();
    }

    private boolean connectToEasyLink(final String elkSSID) {
        mResultCode = RESULT_CONNECTING_DEV;
        boolean isconnected = false;
        int retry = 10;
        Log.d("doujun","connectToEasyLink.");
        while (retry > 0) {
            try {
                isconnected = mWiFiAdmin.connectToEasyLink(elkSSID);
                if(isconnected) {
                    retry=-1;
                    break;
                }
                Thread.sleep(1000);
            } catch (NetworkErrorException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry--;
        }

        return isconnected;
    }

    /*get the state of device via EasyLinkxxxx*/
    private ReceivedPacket checkDevState() {
        mResultCode = RESULT_CHECKING_DEV_STATE;
        SendPacket packet = new SendPacket();
        packet.initCheckServerStatePacket();
        ReceivedPacket receivedPacket = mDeviceClient.sendCommand(packet);
        return receivedPacket;
    }

    /*share wifi ssid and pwd to device */
    private boolean shareWiFiInfoToDevice() {
        boolean isSharedSuccess = false;
        if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_COMUNICATE);//comunicate
        if (mSharedWiFiSSID == null ||mSharedWiFiPWD == null ||
            mSharedWiFiSSID.isEmpty() || mSharedWiFiPWD.isEmpty()) {
            //initWiFiInfo();
        }
        initWiFiInfo();
        ReceivedPacket checkStatePacket = checkDevState();
        if (checkStatePacket != null && checkStatePacket.getCommand() == 1) {
            mResultCode = RESULT_DEV_HAS_ERROR_WIFI_INFO;
            Log.e(TAG, "WiFi SSID or password is wrong");
            //if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_CONFIRM_AP);//connect fail.
            //if (mCallback != null) {
            //    mCallback.onFinished();
            //}
            //return isSharedSuccess;
            //confirmWifiInfo(checkStatePacket.getSSIDFromPacket(),
            //        checkStatePacket.getPasswordFromPacket());
        }

        mResultCode = RESULT_SHARING_WIFI_INFO;
        SendPacket packet = new SendPacket();
        Log.d(TAG,"shareWiFiInfoToDevice mSharedWiFiSSID:"+this.mSharedWiFiSSID);
        Log.d(TAG,"shareWiFiInfoToDevice mSharedWiFiPWD:"+this.mSharedWiFiPWD);
        packet.initWifiConfigPacket(this.mSharedWiFiSSID, this.mSharedWiFiPWD);
        ReceivedPacket receivedPacket = mDeviceClient.sendCommand(packet);
        if (receivedPacket != null) {
            if (receivedPacket.getCommand() == 2) {
                if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_COMUNICATE_OK);//comunicate
                mResultCode = RESULT_SHARED_WIFI_INFO_OK;
                //config wifi ok
            } else {
                if (mCallback != null) mCallback.onState(BaseDevice.DEVSTATE_AP_COMUNICATE_FAIL);//comunicate
                mResultCode = RESULT_SHARED_WIFI_INFO_ERROR;
                //config wifi fail
            }
        }

        if (mCallback != null) {
            mCallback.onFinished();
        }
        return isSharedSuccess;
    }

    private boolean hasScannedELK() {
        boolean isScannedELK = false;
        Log.d("doujun","scanned:"+isScannedELK);
        Log.d("doujun","scann:"+mELKSSID);
        int retry = 30;
        while (retry > 0) {
            try {
                mWiFiAdmin.collectApInfos();
                mAllAPInfos = mWiFiAdmin.getCollectedApInfos();
                for (AccessPointInfo info : mAllAPInfos) {
                    Log.d("doujun","scanned:"+info.getName());
                    if (info.getName().equalsIgnoreCase(mELKSSID)) {
                        isScannedELK = true;
                        retry=-1;
                        break;
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            retry--;
        }
        return isScannedELK;
    }

    private boolean isELKConnected() {
        boolean isELKConnected = false;
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
            if (wifiInfo.getSSID().equals(mELKSSID)) {
                isELKConnected = true;
            }
        }
        return isELKConnected;
    }

    public void confirmWifiInfo(String oldSSID, String oldPwd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog dialog = builder.create();
        LinearLayout contentView = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.dialog_confirm_wifi_info, null);
        final EditText wifiSSIDEditText = (EditText) contentView.findViewById(R.id.confirm_wifi_ssid);
        final EditText wifiPWDEditText = (EditText) contentView.findViewById(R.id.confirm_wifi_passwd);
        wifiSSIDEditText.setText(oldSSID);
        wifiPWDEditText.setText(oldPwd);

        Button setDateFlowButton = (Button) contentView.findViewById(R.id.confirm_wifi_info_btn);
        setDateFlowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mSharedWiFiSSID = wifiSSIDEditText.getText().toString();
                mSharedWiFiPWD = wifiPWDEditText.getText().toString();
            }
        });

        Log.d(TAG,"confirmWifiInfo mSharedWiFiSSID:"+this.mSharedWiFiSSID);
        Log.d(TAG,"confirmWifiInfo mSharedWiFiPWD:"+this.mSharedWiFiPWD);

        dialog.show();
        Window window = dialog.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        window.setContentView(contentView);
    }
}
