// ELookService.aidl
package com.elook.client;

import com.elook.client.user.UserInfo;
import com.elook.client.user.AccessPointInfo;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.MeasurementRecord;

// Declare any non-default types here with import statements

interface ELookServiceInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    boolean saveWifiInfomartion(inout AccessPointInfo info, String passwd);

    boolean loginWithActivedUser();

    boolean login(String username, String password);

    boolean logout();

    boolean registe(String username, String userpwd);

    boolean addUserExternInfo(String username, String email, String province, String city, String area, String address);

    boolean addMeasurement(String username,int deviceId, int deviceType, int delay);

    boolean delMeasurement(String username,int deviceId);
    /*Update the userInfo of CurrentUser*/
    boolean updateUserInfo(inout UserInfo info);

    /*change the password of currentUser*/
    boolean changePassword(String oldPassword, String keyCode, String newPassword);

    /*update the measurements of current user*/
    boolean updateMeasurements(int uid);

    /*update the measurements records of current measurement*/
    boolean updateMeasurementRecords(int devId);

    boolean fetchMeasurementTimedRecord(int devId, String startTime, String endTime);

    boolean fetchMeasurementList(int devId);

    String checkDeviceState(int devId);

    boolean setDeviceState(int devId, int devState);

    boolean getDeviceInfo(int devId);

    boolean getBitmap(int devId);

    boolean setDeviceConfig(int devId, in Bundle params);

    boolean setDeviceNotUseDay(int devId, in Bundle params);

    boolean setEngneerDelay(int devId, in Bundle params);

    boolean setDeviceDateFlow(int devId, in Bundle params);

    boolean saveUpdelay(int devId, int delay);

    boolean fetchProblemMessage(int uid);

    boolean fetchPushMessage(int uid);

    boolean addPushMessage(int userId, String msg);

    boolean fetchSiglePushMessage(int pushid);

    boolean loadImageFromeServer(String imageUrl, String localImagePath);

    boolean setLocation();

    String getMessageVerifyCode(String phonenumber);

    boolean changPasswdWithPhoneMsg(String phonenumber, String newPasswd);

    void getLocation();
}