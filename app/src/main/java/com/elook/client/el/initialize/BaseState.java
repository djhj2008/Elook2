package com.elook.client.el.initialize;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 5/5/16.
 */
public abstract class BaseState {
    public static final int RESULT_NOT_STARTED = 0;

    /*Stage 1*/
    public static final int RESULT_READY_TO_CONNECT_WIFI = RESULT_NOT_STARTED + 1;
    public static final int RESULT_CONNECTING_WIFI = RESULT_NOT_STARTED + 2;
    public static final int RESULT_CONNECTED_WIFI_ERROR = RESULT_NOT_STARTED + 3;
    public static final int RESULT_CONNECTED_WIFI_OK = RESULT_NOT_STARTED + 4;

    /*Stage 2*/
    public static final int RESULT_READY_TO_CONNECT_DEV = RESULT_NOT_STARTED + 5;
    public static final int RESULT_CONNECTING_DEV = RESULT_NOT_STARTED + 6;
    public static final int RESULT_CONNECTED_DEV_ERROR = RESULT_NOT_STARTED + 7;
    public static final int RESULT_CONNECTED_DEV_OK = RESULT_NOT_STARTED + 8;

    public static final int RESULT_CHECKING_DEV_STATE = RESULT_NOT_STARTED + 9;
    public static final int RESULT_DEV_HAS_ERROR_WIFI_INFO = RESULT_NOT_STARTED +10;

    public static final int RESULT_SHARING_WIFI_INFO = RESULT_NOT_STARTED + 12;
    public static final int RESULT_SHARED_WIFI_INFO_ERROR = RESULT_NOT_STARTED + 13;
    public static final int RESULT_SHARED_WIFI_INFO_OK = RESULT_NOT_STARTED + 14;

    /*Stage 3*/
    public static final int RESULT_READY_TO_RECONNECT_WIFI = RESULT_NOT_STARTED + 15;
    public static final int RESULT_RECONNECTING_WIFI = RESULT_NOT_STARTED + 16;
    public static final int RESULT_RECONNECTED_WIFI_ERROR = RESULT_NOT_STARTED + 17;
    public static final int RESULT_RECONNECTED_WIFI_OK = RESULT_NOT_STARTED + 18;

    /*Stage 4*/
    public static final int RESULT_READY_TO_CHECK_DEV_INITIALIZED_STATE = RESULT_NOT_STARTED + 19;
    public static final int RESULT_CHECKING_DEV_INITIALIZED_STATE = RESULT_NOT_STARTED + 20;
    public static final int RESULT_CHECKED_DEV_INITIALIZED_STATE = RESULT_NOT_STARTED + 21;

    public static final int RESULT_READY_TO_CHANGE_DEVICE_STATE = RESULT_NOT_STARTED + 22;
    public static final int RESULT_CHANGING_DEVICE_STATE = RESULT_NOT_STARTED + 23;
    public static final int RESULT_CHANGED_DEVICE_STATE = RESULT_NOT_STARTED + 24;

    public static final int RESULT_READY_TO_WAITE_DEVSTATE_CHANGE = RESULT_NOT_STARTED + 25;
    public static final int RESULT_WAITTING_DEVSTATE_CHANGE = RESULT_NOT_STARTED + 26;
    public static final int RESULT_DEVSTATE_CHANGED_ERROR = RESULT_NOT_STARTED + 27;
    public static final int RESULT_DEVSTATE_CHANGED_OK = RESULT_NOT_STARTED + 28;

    public static final int RESULT_DEVSTATE_DEVICE_NOT_EXIT = RESULT_NOT_STARTED + 29;
    public static final int RESULT_DEVSTATE_DEVICE_PARSE_ERROR = RESULT_NOT_STARTED + 30;
    public static final int RESULT_DEVSTATE_CONFIRM_FAIL = RESULT_NOT_STARTED + 31;
    public static final int RESULT_DEVSTATE_DEVICE_MISS = RESULT_NOT_STARTED + 32;

    public static final int RESULT_DEVSAP_WIFI_SCAN_ERROR = RESULT_NOT_STARTED + 50;
    public static final int RESULT_DEVSAP_WIFI_CONNECT_ERROR = RESULT_NOT_STARTED + 51;
    public static final int RESULT_DEVSAP_WIFI_COMUNICATE_ERROR = RESULT_NOT_STARTED + 52;

    public static final int RESULT_FINISHED_OK = 1;
    public static final int RESULT_FINISHED_ERROR = - 1;

    protected int mResultCode = RESULT_NOT_STARTED;
    protected BaseStateCallBack mCallback;

    private Context mContext;
    protected  BaseState(Context context){
        this.mContext = context;
    }

    public abstract void start();

    public int getResultCode(){
        return mResultCode;
    }

    public abstract void stopGetThread();

    public void registeCallbacks(BaseStateCallBack callBack){
        this.mCallback = callBack;
    }

    public void unregisteCallback(BaseStateCallBack callBack){
        this.mCallback = null;
    }

    public interface BaseStateCallBack {
        void onStart();
        void onFinished();
        void onState(int state);
        void onSubState(int count);
    }
}
