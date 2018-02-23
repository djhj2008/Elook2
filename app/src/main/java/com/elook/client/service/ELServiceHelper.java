package com.elook.client.service;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.elook.client.ELookServiceInterface;
import com.elook.client.user.AccessPointInfo;
import com.elook.client.user.UserInfo;

import java.util.Map;

/**
 * Created by haiming on 3/28/16.
 */
public class ELServiceHelper {
    private static final String TAG ="EasyLookInstanceHelper";

    private static ELookServiceInterface mService = null;
    private static ELServiceHelper sEasyLookHelperInstance = null;

    private static Object mLock = new Object();

    public ELServiceHelper(){};

    public ELServiceHelper(ELookServiceInterface service){
        setService(service);
    }

    public static ELServiceHelper get(){
        if(sEasyLookHelperInstance == null){
            sEasyLookHelperInstance = new ELServiceHelper();
        }
        synchronized (mLock){
            try {
                if( mService == null) mLock.wait();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        return sEasyLookHelperInstance;
    }

    public void setService(ELookServiceInterface service){
        synchronized (mLock){
            if(service != null ){
                mService = service;
                mLock.notify();
            }
        }
    }
    
    public boolean loginWithActivedUser()  {
        boolean ret = false;
        try {
            ret = mService.loginWithActivedUser();
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean login(String username, String password) {
        boolean ret = false;
        try {
            ret = mService.login(username, password);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }


    public boolean logout() {
        boolean ret = false;
        try {
            ret = mService.logout();
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }


    public boolean registe(String username, String userpwd) {
        boolean ret = false;
        try {
            ret = mService.registe(username, userpwd);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean addUserExternInfo(String username, String email, String province,
                                     String city, String area, String address){
        boolean ret = false;
        try {
            ret = mService.addUserExternInfo(username, email, province, city, area, address);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean addMeasurement(String username,int deviceId, int deviceType, int delay){
        boolean ret = false;
        try {
            ret = mService.addMeasurement(username, deviceId, deviceType, delay);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean delMeasurement(String username,int deviceId){
        boolean ret = false;
        try {
            ret = mService.delMeasurement(username, deviceId);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean updateUserInfo(UserInfo info) {
        boolean ret = false;
        try {
            ret = mService.updateUserInfo(info);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }


    public boolean changePassword(String oldPassword, String keyCode, String newPassword) {
        boolean ret = false;
        try {
            ret = mService.changePassword(oldPassword, keyCode, newPassword);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }


    public boolean updateMeasurements(int uid) {
        boolean ret = false;
        try {
            ret = mService.updateMeasurements(uid);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        if(!ret){
            Log.e("ELookInstance", "UpdateFailed");
        }
        return ret;
    }


    public boolean updateMeasurementRecords(int devId) {
        boolean ret = false;
        try {
            ret = mService.updateMeasurementRecords(devId);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        if(!ret){
            Log.e("ELookInstance", "UpdateFailed");
        }
        return ret;
    }

    public boolean fetchMeasurementTimedRecord(int devId, String startTime, String endTime){
        boolean ret = false;
        try {
            ret = mService.fetchMeasurementTimedRecord(devId, startTime, endTime);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        if(!ret){
            Log.e("ELookInstance", "UpdateFailed");
        }
        return ret;

    }

    public boolean fetchMeasurementList(int devId){
        boolean ret = false;
        try {
            ret = mService.fetchMeasurementList(devId);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        if(!ret){
            Log.e("ELookInstance", "UpdateFailed");
        }
        return ret;

    }

    public String checkDeviceState(int devId){
        String ret = "";
        try {
            ret = mService.checkDeviceState(devId);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean setDeviceState(int devId, int devState){
        boolean ret = false;
        try {
            ret = mService.setDeviceState(devId, devState);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean getDeviceInfo(int devId){
        boolean ret = false;
        try {
            ret = mService.getDeviceInfo(devId);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean getBitmap(int devId){
        return false;
    }

    public boolean saveWifiInfomartion(AccessPointInfo info, String passwd){
        boolean ret = false;
        try {
            ret = mService.saveWifiInfomartion(info, passwd);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean setDeviceConfig(int devId, Bundle params){
        boolean ret = false;
        try {
            ret = mService.setDeviceConfig(devId, params);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean setDeviceNotUseDay(int devId, Bundle params){
        boolean ret = false;
        try {
            ret = mService.setDeviceNotUseDay(devId, params);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean setEngneerDelay(int devId, Bundle params){
        boolean ret = false;
        try {
            ret = mService.setEngneerDelay(devId, params);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean setDeviceDateFlow(int devId, Bundle params){
        boolean ret = false;
        try {
            ret = mService.setDeviceDateFlow(devId, params);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean saveUpdelay(int devId, int delay){
        boolean ret = false;
        try {
            ret = mService.saveUpdelay(devId, delay);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean fetchProblemMessage(int uid){
        boolean ret = false;
        try {
            ret = mService.fetchProblemMessage(uid);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean fetchPushMessage(int uid){
        boolean ret = false;
        try {
            ret = mService.fetchPushMessage(uid);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean fetchSiglePushMessage(int pushid){
        boolean ret = false;
        try {
            ret = mService.fetchSiglePushMessage(pushid);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean loadImageFromeServer(String imageUrl, String localImagePath){
        boolean ret = false;
        try {
           ret = mService.loadImageFromeServer(imageUrl, localImagePath);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public boolean setLocation(){
        boolean ret = false;
        try {
            ret = mService.setLocation();
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }


    public String getMessageVerifyCode(String phonenumber){
        String verifyCodeInfo = "";
        try {
            verifyCodeInfo = mService.getMessageVerifyCode(phonenumber);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return verifyCodeInfo;
    }

    public boolean changPasswdWithPhoneMsg(String phonenumber, String newPasswd){
        boolean ret = false;
        try {
            ret = mService.changPasswdWithPhoneMsg(phonenumber,newPasswd);
        } catch (RemoteException e){
            e.printStackTrace();
        }
        return ret;
    }

    public void getLocation(){
        try {
            mService.getLocation();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}