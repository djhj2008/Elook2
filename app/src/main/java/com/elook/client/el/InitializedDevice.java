package com.elook.client.el;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.elook.client.R;
import com.elook.client.el.initialize.BaseDevice;
import com.elook.client.el.initialize.BaseState;
import com.elook.client.el.initialize.ConnectToMeasurementStatte;
import com.elook.client.el.initialize.ConnectToServerState;
import com.elook.client.el.initialize.ConnectToWiFiState;
import com.elook.client.el.initialize.ReconnectToWiFiState;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.user.MeasurementInfo;

/**
 * Created by guoguo on 4/2/2016.
 */
public class InitializedDevice{
    private static final String TAG = "InitializedDevice";
    private static final int TIME_OUT = 3 * 60 * 1000;
    private static final int LAYER_DELAY = 500;
    private static final int WIFI_DELAY = 1000;
    private static final int RECONNECT_WIFI_COUNT_MAX = 5;

    int mDeviceId = 123456788;
    int layercount = 0;
    Context mContext;
    View mMainView = null;
    Button mStartInitDevice;
    int mDevState = 0;
    int mDevAPState = 0;
    BaseState mConnectToWifiState, mConnectToDevState,mReconnectToWiFiState, mConnectToServerState;
    BaseState mCurrentState;
    ImageView mInitDevImageView;
    TextView mInitDevTextView;
    TextView mInitDevTextCountView;
    TextView mProcessTextView;
    ProgressBar mProcessBar;
    int mCurProcessValue=0;
    int mProcessDelay=500;
    int reconnectWifiCount = 0;
    int[] mBgLayer=new int[]{
            R.drawable.bg_layer1,
            R.drawable.bg_layer2,
            R.drawable.bg_layer3,
            R.drawable.bg_layer4,
    };

    int mStartStateIndex = 0;
    String mDevWaterType;
    OnInitializedDeviceListener mListener;

    public InitializedDevice(Context context, int deviceId){
        this(context, deviceId, 0);
    }

    public InitializedDevice(Context context, int deviceId, int state){
        mContext = context;
        this.mDeviceId = deviceId;
        //this.mStartStateIndex = startIndex;
        mDevWaterType = String.valueOf(mDeviceId).substring(0,1);
        mDevState = state;
        if(mDevWaterType.equals(BaseDevice.DEVICE_WATER_WIFI)){
            if(mDevState  >= MeasurementInfo.MEASUREMENT_STATE_CONNECT
                    &&mDevState  != MeasurementInfo.MEASUREMENT_STATE_NET_ERROR){
                mStartStateIndex = 3;
            }else {
                mStartStateIndex = 0;
            }
        }else if(mDevWaterType.equals(BaseDevice.DEVICE_WATER_GPRS)){
            mStartStateIndex = 3;
        }
        mConnectToWifiState = new ConnectToWiFiState(context);
        mConnectToDevState = new ConnectToMeasurementStatte(context, deviceId);
        mReconnectToWiFiState = new ReconnectToWiFiState(context);
        mConnectToServerState = new ConnectToServerState(context, deviceId);

        mCurrentState = mConnectToWifiState;
        initViews();
    }

    private void initViews(){
        mMainView = LayoutInflater.from(mContext).inflate(R.layout.init_device_main, null);

        mInitDevImageView = (ImageView)mMainView.findViewById(R.id.init_dev_layer);
        mInitDevTextView = (TextView)mMainView.findViewById(R.id.init_dev_text);
        mInitDevTextCountView = (TextView)mMainView.findViewById(R.id.init_dev_count_text);
        mStartInitDevice = (Button)mMainView.findViewById(R.id.auto_init_dev);
        mStartInitDevice.setOnClickListener(mStartInitDevButtonListener);
        mProcessBar = (ProgressBar)mMainView.findViewById(R.id.config_progressBar);
        mProcessTextView = (TextView)mMainView.findViewById(R.id.init_process_text);
        reset();
    }

    public void setProcessBar(int value){
        mCurProcessValue = value;
        mProcessDelay = 50;
        if(value==0){
            mProcessTextView.setText(0 + "%");
            mProcessBar.setProgress(0);
        }else if(value == 100){
            mProcessTextView.setText(100 + "%");
            mProcessBar.setProgress(100);
        }else {
            if (mSubHandler != null) {
                mSubHandler.post(mProcessRunnable);
            }
        }
    }


    public void setProcessBar(int value,int delay){
        int pre = mProcessBar.getProgress();
        int delay2=0;
        delay2 = (mCurProcessValue - pre) *55;
        Log.d(TAG," setProcessBar2 pre:"+pre);
        Log.d(TAG," setProcessBar2 mCurProcessValue:"+mCurProcessValue);
        //Log.d(TAG," setProcessBar2 mProcessDelay:"+mProcessDelay);
        mCurProcessValue = value;
        //mProcessDelay = delay;
        if(value==0){
            mProcessTextView.setText(0 + "%");
            mProcessBar.setProgress(0);
        }else {
            if (mSubHandler != null) {
                mSubHandler.postDelayed(mProcessRunnableDelay, delay2);
            }
        }
    }

    Runnable mProcessRunnable=new Runnable(){
        public void run() {
            if (mSubHandler != null) {
                mSubHandler.removeCallbacks(mProcessRunnable);
                int cur = mProcessBar.getProgress();
                //Log.d(TAG," mProcessRunnable cur:"+cur);
                if (cur < mCurProcessValue) {
                    cur++;
                    mProcessTextView.setText(cur + "%");
                    mProcessBar.setProgress(cur);
                    mSubHandler.postDelayed(mProcessRunnable, mProcessDelay);
                }
            }
        }
    };

    Runnable mProcessRunnableDelay=new Runnable(){
        public void run() {
            if (mSubHandler != null) {
                mSubHandler.removeCallbacks(mProcessRunnable);
                mProcessDelay = 2500;
                mSubHandler.post(mProcessRunnable);
            }
        }
    };


    public void stopProcessBar(){
        if(mSubHandler!=null) {
            if (mProcessRunnable != null)
                mSubHandler.removeCallbacks(mProcessRunnable);
            if (mProcessRunnableDelay != null)
                mSubHandler.removeCallbacks(mProcessRunnableDelay);
        }
    }

    public void stopConfig(){
        removeMainMsg(MSG_TIMEOUT);
        mStartInitDevice.setEnabled(true);
        removeMainMsg(MSG_UPDATA_DEV_LAYER);
        stopProcessBar();
    }

    public static final int MSG_SUB_CONNECT_TO_WIFI = 1000;
    public static final int MSG_SUB_DEVAP_PROCESS_ACTION = 1001;
    public static final int MSG_SUB_CONNECT_TO_SERVER_COUNT = 1002;

    private Handler mSubHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUB_CONNECT_TO_WIFI:
                    Log.d(TAG," MSG_SUB_CONNECT_TO_WIFI arg:"+msg.arg1);
                    mDevState = msg.arg1;
                    if(mDevState==MeasurementInfo.MEASUREMENT_STATE_DEV_MIS){
                        mInitDevTextView.setText(R.string.config_dev_wait_confirm);
                        setProcessBar(80);
                        setProcessBar(99,2000);
                    }else if(mDevState == MeasurementInfo.MEASUREMENT_STATE_CONFIG_LED){
                        mInitDevTextView.setText(R.string.config_dev_led_config);
                        setProcessBar(65);
                        setProcessBar(99,2000);
                    }else if(mDevState == MeasurementInfo.MEASUREMENT_STATE_CONNECT){
                        mInitDevTextView.setText(R.string.config_dev_config_pre_start);
                    }
                    break;
                case MSG_SUB_DEVAP_PROCESS_ACTION:
                    Log.d(TAG," MSG_SUB_DEVAP_PROCESS_ACTION arg:"+msg.arg1);
                    mDevAPState = msg.arg1;
                    showDevAPStateString(mDevAPState);
                    break;
                case MSG_SUB_CONNECT_TO_SERVER_COUNT:
                    String notify;
                    int count = msg.arg1;
                    if(count >0) {
                        notify = mContext.getString(R.string.config_dev_connect_server_count)+count;
                        mInitDevTextCountView.setVisibility(View.VISIBLE);
                        mInitDevTextCountView.setText(notify);
                    }else{
                        mInitDevTextCountView.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };

    public void showDevAPStateString(int state){
        switch (state){
            case BaseDevice.DEVSTATE_AP_SCAN:
                mInitDevTextView.setText(R.string.config_dev_ap_scan);
                setProcessBar(28);
                setProcessBar(99,2000);
                break;
            case BaseDevice.DEVSTATE_AP_SCAN_OK:
                mInitDevTextView.setText(R.string.config_dev_ap_scan_ok);
                setProcessBar(30);
                setProcessBar(99,2000);
                break;
            case BaseDevice.DEVSTATE_AP_SCAN_FAIL:
                mInitDevTextView.setText(R.string.config_dev_ap_scan_fail);
                stopConfig();
                break;
            case BaseDevice.DEVSTATE_AP_CONNECT:
                mInitDevTextView.setText(R.string.config_dev_ap_connect);
                break;
            case BaseDevice.DEVSTATE_AP_CONNECT_OK:
                mInitDevTextView.setText(R.string.config_dev_ap_connect_ok);
                setProcessBar(32);
                setProcessBar(99,2000);
                break;
            case BaseDevice.DEVSTATE_AP_CONNECT_FAIL:
                mInitDevTextView.setText(R.string.config_dev_ap_connect_fail);
                stopConfig();
                break;
            case BaseDevice.DEVSTATE_AP_CONFIRM_AP:
                mInitDevTextView.setText(R.string.config_dev_ap_confirm_ap);
                stopConfig();
                break;
            case BaseDevice.DEVSTATE_AP_COMUNICATE:
                mInitDevTextView.setText(R.string.config_dev_ap_comunicate);
                break;
            case BaseDevice.DEVSTATE_AP_COMUNICATE_OK:
                mInitDevTextView.setText(R.string.config_dev_ap_comunicate_ok);
                setProcessBar(34);
                setProcessBar(99,2000);
                break;
            case BaseDevice.DEVSTATE_AP_COMUNICATE_FAIL:
                mInitDevTextView.setText(R.string.config_dev_ap_comunicate_fail);
                stopConfig();
                break;
        }
    }


    public void setInitializedListener(OnInitializedDeviceListener listener){
        mListener = listener;
    }

    View.OnClickListener mStartInitDevButtonListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            resetViews();
            mStartInitDevice.setEnabled(false);
            startConfigurate();
        }
    };

    public View getInitDeviceMainView(){
        if(mMainView == null){
            initViews();
        }
        return mMainView;
    }

    private void resetViews() {

    }

    private void startToConnectWiFi(){
        sendMainMsgDelay(MSG_TIMEOUT, TIME_OUT);
        mInitDevTextView.setText(R.string.connected_to_wifi);
        mCurrentState = mConnectToWifiState;
        setProcessBar(5);
        setProcessBar(99,2000);
        mConnectToWifiState.registeCallbacks(new BaseState.BaseStateCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinished() {
                int resultCode = mConnectToWifiState.getResultCode();
                if(resultCode == BaseState.RESULT_CONNECTED_WIFI_OK ||
                        resultCode == BaseState.RESULT_CONNECTED_WIFI_ERROR){
                    sendMainMsg(MSG_FINISHED_CONNECT_WIFI);
                } else {
                    Log.e(TAG, "Error, state: "+resultCode);
                }

            }

            @Override
            public void onState(int state) {

            }

            @Override
            public void onSubState(int count) {

            }
        });
        mConnectToWifiState.start();
    }

    private void startToConnectDevice(){
        sendMainMsgDelay(MSG_TIMEOUT, TIME_OUT);
        mInitDevTextView.setText(R.string.connected_to_device);
        setProcessBar(25);
        setProcessBar(99,2000);
        mCurrentState = mConnectToDevState;
        mConnectToDevState.registeCallbacks(new BaseState.BaseStateCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinished() {
                int resultCode = mConnectToDevState.getResultCode();
                if(resultCode == BaseState.RESULT_SHARED_WIFI_INFO_ERROR ||
                        resultCode == BaseState.RESULT_SHARED_WIFI_INFO_OK ){
                    sendMainMsg(MSG_FINISHED_CONNECT_DEV);
                } else {
                    Log.e(TAG, "Error, state: "+resultCode);
                }
            }

            @Override
            public void onState(int state) {
                Log.d(TAG,"startToConnectDevice state:"+state);
                mDevAPState = state;
                if(mSubHandler!=null) {
                    Message msg = new Message();
                    msg.what = MSG_SUB_DEVAP_PROCESS_ACTION;
                    msg.arg1 = state;
                    mSubHandler.sendMessage(msg);
                }
            }

            @Override
            public void onSubState(int count) {

            }
        });
        mConnectToDevState.start();
    }

    private void startToReconnectWiFi(){
        sendMainMsgDelay(MSG_TIMEOUT, TIME_OUT);
        mInitDevTextView.setText(R.string.start_to_reconnect_to_wifi);
        setProcessBar(35);
        setProcessBar(99,2000);
        mCurrentState = mReconnectToWiFiState;
        reconnectWifiCount++;
        mReconnectToWiFiState.registeCallbacks(new BaseState.BaseStateCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinished() {
                int resultCode = mReconnectToWiFiState.getResultCode();
                if(resultCode == BaseState.RESULT_CONNECTED_WIFI_OK){
                    sendMainMsg(MSG_FINISHED_RECONNECT_WIFI);
                    setProcessBar(40);
                    setProcessBar(99,2000);
                }else if (resultCode == BaseState.RESULT_CONNECTED_WIFI_ERROR){
                    if(reconnectWifiCount>5){
                        sendMainMsg(MSG_FINISHED_RECONNECT_WIFI);
                        setProcessBar(40);
                        setProcessBar(99,2000);
                    }else {
                        sendMainMsgDelay(MSG_START_RECONNECT_WIFI, WIFI_DELAY);
                    }
                }else {
                    Log.e(TAG, "Error, state: "+resultCode);
                }
            }

            @Override
            public void onState(int state) {

            }

            @Override
            public void onSubState(int count) {

            }
        });
        mReconnectToWiFiState.start();
    }

    private void startToVerifyInitializedState(){
        sendMainMsgDelay(MSG_TIMEOUT, TIME_OUT);
        mInitDevTextView.setText(R.string.verify_init_dev_success);
        mCurrentState = mConnectToServerState;
        setProcessBar(99,2000);

        mConnectToServerState.registeCallbacks(new BaseState.BaseStateCallBack() {
            @Override
            public void onStart() {

            }

            @Override
            public void onFinished() {
                int resultCode = mConnectToServerState.getResultCode();
                if(resultCode == BaseState.RESULT_DEVSTATE_CHANGED_ERROR ||
                        resultCode == BaseState.RESULT_DEVSTATE_CHANGED_OK ||
                        resultCode == BaseState.RESULT_DEVSTATE_DEVICE_NOT_EXIT ||
                        resultCode == BaseState.RESULT_DEVSTATE_DEVICE_PARSE_ERROR||
                        resultCode == BaseState.RESULT_DEVSTATE_CONFIRM_FAIL){
                    sendMainMsg(MSG_FINISHED_VERIFY_INITIALIZED_STATE);
                } else {
                    Log.e(TAG, "Error, state: "+resultCode);
                }
            }

            @Override
            public void onState(int state) {
                Log.d(TAG," onState:"+state);
                mDevState = state;
                //if(state==MeasurementInfo.MEASUREMENT_STATE_DEV_MIS){
                    Message msg = new Message();
                    if (mSubHandler != null) {
                        msg.what = MSG_SUB_CONNECT_TO_WIFI;
                        msg.arg1 = state;
                        mSubHandler.sendMessage(msg);
                    }
                //}
            }

            @Override
            public void onSubState(int count) {
                    Message msg = new Message();
                    Log.d(TAG,"onSubState:"+count);
                    if (mSubHandler != null) {
                        msg.what = MSG_SUB_CONNECT_TO_SERVER_COUNT;
                        msg.arg1 = count;
                        mSubHandler.sendMessage(msg);
                    }
            }


        });
        Log.d(TAG,"startToVerifyInitializedState mDevState:"+mDevState);
        if(mDevState == MeasurementInfo.MEASUREMENT_STATE_CANNOT_PARSE
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_PARSE_FAIL
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_CONNECT
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_DEV_MIS
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_CONFIG_LED) {
            ELServiceHelper helper = ELServiceHelper.get();
            helper.setDeviceState(mDeviceId, 2);
        }
        mConnectToServerState.start();

    }

    private void startConfigurate(){
        if(mDevState == MeasurementInfo.MEASUREMENT_STATE_CONNECT
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_START_CONFIG
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_CANNOT_PARSE
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_PARSE_FAIL
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_DEV_MIS
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_CONFIG_LED){
            mStartStateIndex = 3;
        }
        else{
            mStartStateIndex = 0;
        }

        if(mDevWaterType.equals(BaseDevice.DEVICE_WATER_GPRS)){
            mStartStateIndex = 3;
        }
        setProcessBar(0);
        setProcessBar(99,2000);

        if(mStartStateIndex == 0){
            sendMainMsg(MSG_START_CONFIG);
        } else if(mStartStateIndex  == 1){
            sendMainMsg(MSG_START_CONNECT_DEV);
        } else if(mStartStateIndex == 2){
            sendMainMsgDelay(MSG_START_RECONNECT_WIFI,WIFI_DELAY);

        } else if(mStartStateIndex == 3){
            setProcessBar(40);
            setProcessBar(99,2000);
            sendMainMsg(MSG_START_VERIFY_INITIALIZED_STATE);
        }
        sendMainMsgDelay(MSG_UPDATA_DEV_LAYER,LAYER_DELAY);
    }

    public static final int MSG_START_CONFIG = 0;

    public static final int MSG_START_CONNECT_WIFI = 1;
    public static final int MSG_FINISHED_CONNECT_WIFI = 2;


    public static final int MSG_START_CONNECT_DEV = 3;
    public static final int MSG_FINISHED_CONNECT_DEV = 4;


    public static final int MSG_START_RECONNECT_WIFI = 5;
    public static final int MSG_FINISHED_RECONNECT_WIFI = 6;


    public static final int MSG_START_VERIFY_INITIALIZED_STATE = 7;
    public static final int MSG_FINISHED_VERIFY_INITIALIZED_STATE = 8;

    public static final int MSG_FINISHED_VERIFY_ERROR_STATE = 9;

    public static final int MSG_FINISHED_CONFIRM_ERROR_STATE = 10;

    public static final int MSG_DEVAP_SCAN_ERROR_STATE = 11;
    public static final int MSG_DEVAP_CONNECT_ERROR_STATE = 12;
    public static final int MSG_DEVAP_COMUNICATE_ERROR_STATE = 13;

    public static final int MSG_UPDATA_DEV_LAYER = 101;

    public static final int MSG_TIMEOUT = 100 ;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int resultCode = mCurrentState.getResultCode();
            switch (msg.what) {
                case MSG_START_CONFIG:
                case MSG_START_CONNECT_WIFI:
                    startToConnectWiFi();
                    break;
                case MSG_FINISHED_CONNECT_WIFI:
                    removeMainMsg(MSG_TIMEOUT);
                    if (resultCode == BaseState.RESULT_CONNECTED_WIFI_OK) {
                        mInitDevTextView.setText(R.string.please_connected_to_wifi);
                        sendMainMsg(MSG_START_CONNECT_DEV);
                    } else {
                        stopConfig();
                        if (mListener != null)
                            mListener.onInitializedDevice(MSG_FINISHED_CONNECT_WIFI, false);
                        Log.e(TAG, "Cannot connect to WiFi");
                    }

                    break;

                case MSG_START_CONNECT_DEV:
                    startToConnectDevice();
                    break;
                case MSG_FINISHED_CONNECT_DEV:
                    removeMainMsg(MSG_TIMEOUT);
                    if (resultCode == BaseState.RESULT_SHARED_WIFI_INFO_OK) {
                        sendMainMsgDelay(MSG_START_RECONNECT_WIFI,WIFI_DELAY);
                        reconnectWifiCount = 0;
                    } else {
                        stopConfig();
                        if (mListener != null)
                            mListener.onInitializedDevice(MSG_FINISHED_CONNECT_DEV, false);
                            Log.e(TAG, "Cannot initialized the Device");
                    }

                    break;
                case MSG_START_RECONNECT_WIFI:
                    startToReconnectWiFi();
                    break;
                case MSG_FINISHED_RECONNECT_WIFI:
                    removeMainMsg(MSG_TIMEOUT);
                    if (resultCode == BaseState.RESULT_CONNECTED_WIFI_OK) {
                        sendMainMsg(MSG_START_VERIFY_INITIALIZED_STATE);
                    } else {
                        stopConfig();
                        if (mListener != null)
                            mListener.onInitializedDevice(MSG_FINISHED_RECONNECT_WIFI, false);
                        Log.e(TAG, "Cannot reconnect to WiFi");
                    }

                    break;
                case MSG_START_VERIFY_INITIALIZED_STATE:
                    startToVerifyInitializedState();
                    break;
                case MSG_FINISHED_VERIFY_INITIALIZED_STATE:
                    setProcessBar(100);
                    removeMainMsg(MSG_TIMEOUT);
                    removeMainMsg(MSG_UPDATA_DEV_LAYER);
                    if (resultCode == BaseState.RESULT_DEVSTATE_CHANGED_OK) {
                        if (mListener != null)
                            mListener.onInitializedDevice(MSG_FINISHED_VERIFY_INITIALIZED_STATE, true);
                    }
                    else if (resultCode == BaseState.RESULT_DEVSTATE_DEVICE_NOT_EXIT){
                        if(mListener != null)mListener.onInitializedDevice(MSG_FINISHED_VERIFY_ERROR_STATE, true);
                    }else if (resultCode == BaseState.RESULT_DEVSTATE_DEVICE_PARSE_ERROR){
                        mDevState = MeasurementInfo.MEASUREMENT_STATE_CANNOT_PARSE;
                        if(mListener != null)mListener.onInitializedDevice(MSG_FINISHED_VERIFY_INITIALIZED_STATE, false);
                    }else if(resultCode == BaseState.RESULT_DEVSTATE_CONFIRM_FAIL){
                        mDevState = MeasurementInfo.MEASUREMENT_STATE_PARSE_FAIL;
                        if(mListener != null)mListener.onInitializedDevice(MSG_FINISHED_CONFIRM_ERROR_STATE, false);
                    }
                    else {
                        if(mListener != null)mListener.onInitializedDevice(MSG_FINISHED_VERIFY_INITIALIZED_STATE, false);
                    }
                    stopThread();
                    stopProcessBar();
                    if(resultCode != BaseState.RESULT_DEVSTATE_CHANGED_OK){
                        reset();
                    }
                    mStartInitDevice.setEnabled(true);
                    break;

                case MSG_TIMEOUT:
                    removeMainMsg(MSG_UPDATA_DEV_LAYER);
                    mConnectToServerState.stopGetThread();
                    stopConfig();
                    if(mListener != null)mListener.onInitializedDevice(MSG_TIMEOUT, false);
                    //mInitDevTextView.setText("Time Out!");
                    mStartInitDevice.setEnabled(true);
                    reset();
                    break;

                case MSG_UPDATA_DEV_LAYER:
                    layercount++;
                    mInitDevImageView.setImageResource(mBgLayer[layercount%4]);
                    sendMainMsgDelay(MSG_UPDATA_DEV_LAYER,LAYER_DELAY);
                    break;
            }
        }
    };

    public void sendMainMsg(int what){
        if(mHandler!=null){
            mHandler.sendEmptyMessage(what);
        }

    }

    public void removeMainMsg(int what){
        if(mHandler!=null){
            mHandler.removeMessages(what);
        }

    }

    public void sendMainMsgDelay(int what,int delay){
        if(mHandler!=null){
            mHandler.sendEmptyMessageDelayed(what,delay);
        }

    }

    private void reset(){
        Log.d(TAG,"reset:"+mDevState);
        removeMainMsg(MSG_TIMEOUT);
        if(mDevState == MeasurementInfo.MEASUREMENT_STATE_CONNECT
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_START_CONFIG
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_CANNOT_PARSE
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_PARSE_FAIL) {
            mInitDevTextView.setText(R.string.restart_init_device);
        }else if(mDevState == MeasurementInfo.MEASUREMENT_STATE_DEV_MIS
                ||mDevState == MeasurementInfo.MEASUREMENT_STATE_CONFIG_LED) {
            mInitDevTextView.setText(R.string.restart_init_device2);
        }else if(mDevState == MeasurementInfo.MEASUREMENT_STATE_NET_ERROR) {
            mInitDevTextView.setText(R.string.restart_init_device7);
        } else {
            mInitDevTextView.setText(R.string.auto_init_device);
        }
        setProcessBar(0);
        mStartInitDevice.setEnabled(true);
    }

    public void restart(){
        //mInitDevTextView.setText(R.string.auto_init_device);
        mStartInitDevice.setEnabled(true);
    }
    
    public void stopThread() {
        mConnectToWifiState.stopGetThread();
        mConnectToDevState.stopGetThread();
        mReconnectToWiFiState.stopGetThread();
    }

    public void clean() {
        removeMainMsg(MSG_TIMEOUT);
        if(mSubHandler!=null) {
            mSubHandler.removeCallbacks(mProcessRunnable);
            mSubHandler = null;
        }
        mHandler=null;
    }

    public interface OnInitializedDeviceListener{
        public void onInitializedDevice(int initializeStage, boolean isSuccessfully);
    }
}
