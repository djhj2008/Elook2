package com.elook.client.el.initialize;

import android.content.Context;
import android.os.Handler;
import android.os.Message;


import com.elook.client.el.InitializedDevice;
import com.elook.client.service.DeviceStatePacket;
import com.elook.client.service.ELServiceHelper;
import com.elook.client.user.MeasurementInfo;

/**
 * Created by haiming on 5/5/16.
 */
public class ConnectToServerState extends BaseState {
    private static final String TAG = "ConnectToServerState";

    int mDeviceId;
    private Context mContext;
    DeviceStateInServerThread mCheckStateThread;

    public ConnectToServerState(Context context, int devId) {
        super(context);
        mContext = context;
        this.mDeviceId = devId;
        mResultCode = RESULT_READY_TO_CHECK_DEV_INITIALIZED_STATE;
    }

    @Override
    public void start() {
        if(mCallback != null)mCallback.onStart();
        DeviceStateInServerListener stateChangeListener = new DeviceStateInServerListener() {
            @Override
            public void onDeviceStateChangedInServer(int devId, int currentState) {
                ELServiceHelper helper;
                boolean ret = false;
                switch (currentState){
                    case BaseDevice.DEVSTATE_PRE_INIT:
                        break;
                    case BaseDevice.DEVSTATE_HAS_CONNECT_SERVER:
                        mResultCode = RESULT_CHANGING_DEVICE_STATE;
                        helper = ELServiceHelper.get();
                        ret = helper.setDeviceState(mDeviceId, 2);
//                        if(ret.equals("OK2")){
//                            mResultCode = RESULT_CHANGED_DEVICE_STATE;
//                        } else {
//                            mResultCode = RESULT_DEVSTATE_CHANGED_ERROR;
//                        }
                        break;
                    case BaseDevice.DEVSTATE_DIG_PARSE_FAIL:
                        mResultCode = RESULT_DEVSTATE_DEVICE_PARSE_ERROR;
                        break;
                    case BaseDevice.DEVSTATE_CONFIG_PASS:
                        mResultCode = RESULT_DEVSTATE_CHANGED_OK;
                        break;
                    case BaseDevice.DEVSTATE_CONFIRM_FAIL:
                        mResultCode = RESULT_DEVSTATE_CONFIRM_FAIL;
                        break;
                    case BaseDevice.DEVSTATE_DEVID_NOT_EXISTED:
                        mResultCode = RESULT_DEVSTATE_DEVICE_NOT_EXIT;
                        break;
                    case BaseDevice.DEVSTATE_DEV_CONFIG_MIS:
                        mResultCode = RESULT_DEVSTATE_DEVICE_MISS;
                        break;
                    default:
                        mResultCode = RESULT_DEVSTATE_CHANGED_ERROR;
                        break;
                }
            }
        };

        mCheckStateThread = new DeviceStateInServerThread(mDeviceId, stateChangeListener);
        mCheckStateThread.start();
    }

    /*get the state of device in server*/
    private int getDeviceStateInServer(final int devid) {
        try {
            Thread.sleep(30 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ELServiceHelper helper = ELServiceHelper.get();
        String result = helper.checkDeviceState(mDeviceId);
        DeviceStatePacket packet = new DeviceStatePacket(result);
        return packet.getDevState();
    }

    @Override
    public void stopGetThread(){
        if(mCheckStateThread!=null&&mCheckStateThread.isAlive()){
            mCheckStateThread.cancelGetState();
            mCheckStateThread.interrupt();
            mCheckStateThread=null;
        }
    }



    interface DeviceStateInServerListener {
        void onDeviceStateChangedInServer(int devId, int currentState);
    }

    class DeviceStateInServerThread extends Thread {
        private int deviceId;
        private int currentState;
        private int currentStateSub;
        private boolean mCheckLoopStoped = false;
        private DeviceStateInServerListener callBack;
        private int flag=0;

        public  DeviceStateInServerThread(int devId, DeviceStateInServerListener listener){
            this.deviceId = devId;
            this.callBack = listener;
        }
        public  void cancelGetState(){
            flag = 1;
        }

        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ELServiceHelper helper = ELServiceHelper.get();
                String devStateStr = helper.checkDeviceState(deviceId);
                DeviceStatePacket packet = new DeviceStatePacket(devStateStr);
                if(packet.getDevState() != BaseDevice.DEVSTATE_ERROR && currentState != packet.getDevState()){
                    currentState = packet.getDevState();
                    callBack.onDeviceStateChangedInServer(deviceId, currentState);
                    if(currentState == BaseDevice.DEVSTATE_CONFIG_PASS
                            ||currentState == BaseDevice.DEVSTATE_DEVID_NOT_EXISTED
                            ||currentState ==BaseDevice.DEVSTATE_DIG_PARSE_FAIL
                            ||currentState == BaseDevice.DEVSTATE_CONFIRM_FAIL){
                        if(mCallback != null) mCallback.onFinished();
                        break;
                    }else {
                        if (mCallback != null) mCallback.onState(currentState);
                    }
                }else if( packet.getDevState()==BaseDevice.DEVSTATE_START_TO_CONFIG&&currentStateSub != packet.getDevStateSubCout()){
                    currentStateSub = packet.getDevStateSubCout();
                    if (mCallback != null) mCallback.onSubState(packet.getDevStateSubCout());
                }else if(packet.getDevStateSubCout() == 0){
                    if (mCallback != null) mCallback.onSubState(packet.getDevStateSubCout());
                }
                if(flag==1){
                    break;
                }
            }
        }
    }
}
