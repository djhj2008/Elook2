package com.elook.client.service;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;


import com.elook.client.ELookServiceInterface;
import com.elook.client.activity.MainContentActivity;
import com.elook.client.exception.ErrorCode;
import com.elook.client.exception.ErrorCodeMap;
import com.elook.client.exception.ExceptionCenter;
import com.elook.client.location.LocationWrapper;
import com.elook.client.user.AccessPointInfo;
import com.elook.client.user.UserInfo;
import com.elook.client.utils.Constant;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by haiming on 5/16/16.
 */
public class ELookServiceImpl extends ELookServiceInterface.Stub {
    private static final String TAG = "ELookService";
    private static final String SHARE_PREFERENCE_NAME  = "elook_preference";
    public static final String HTTP_PARAMS_ENDUSERNAME = "endusername";
    public static final String HTTP_PARAMS_ENDUSERID = "enduserid";
    public static final String HTTP_PARAMS_ENDUSERPWD = "enduserpwd";
    public static final String HTTP_PARAMS_EMAIL = "mail";
    public static final String HTTP_PARAMS_PROVINCE = "province";
    public static final String HTTP_PARAMS_CITY = "city";
    public static final String HTTP_PARAMS_AREA = "area";
    public static final String HTTP_PARAMS_ADDR = "addr";
    public static final String HTTP_PARAMS_DEV_ID = "deviceid";
    public static final String HTTP_PARAMS_DEV_TYPE = "devicetype";
    public static final String HTTP_PARAMS_DEV_DELAY = "delay";
    public static final String HTTP_PARAMS_DEV_FLOW = "flow";
    public static final String HTTP_PARAMS_DEV_UPLSTATE = "state";
    public static final String HTTP_PARAMS_DEV_INTERNAL = "date";
    public static final String HTTP_PARAMS_DEV_PAGE = "page";
    public static final String HTTP_PARAMS_TIME_START = "start";
    public static final String HTTP_PARAMS_TIME_END = "end";
    public static final String HTTP_PARAMS_ALIAS = "alias";
    public static final String HTTP_PARAMS_LOCATION = "location";
    public static final String HTTP_PARAMS_PAY_ID = "payid";
    public static final String HTTP_PARAMS_DEVICE_SETACCESS = "setaccess";

    private static final int THREAD_POOL_SIZE = 10;
    ExecutorService mThreadPool;
    private Context mContext;
    ELookDatabaseHelper mDatabaseHelper;
    UserInfoService mUserInfoService;
    MeasurementInfoService mMeasurementService;
    MeasurementRecordService mMeasurementRecordService;
    MeasurementListService mMeasurementListService;
    PushMessageService mPushMessageService;
    ProblemMsgService mProblemMessageService;

    public ELookServiceImpl(Context c){
        this.mContext = c;
        mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mDatabaseHelper = ELookDatabaseHelper.newInstance(c);
        initService();
        //new QueryLocationTask().execute();
    }

    private void initService(){
        mUserInfoService = new UserInfoService(mContext,mDatabaseHelper);
        mMeasurementService = new MeasurementInfoService(mContext, mDatabaseHelper);
        mMeasurementRecordService = new MeasurementRecordService(mContext, mDatabaseHelper);
        mPushMessageService = new PushMessageService(mContext, mDatabaseHelper);
        mMeasurementListService = new MeasurementListService(mContext, mDatabaseHelper);
        mProblemMessageService = new ProblemMsgService(mContext,mDatabaseHelper);

    }

    @Override
    public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

    }

    @Override
    public boolean loginWithActivedUser() throws RemoteException {
        Log.d(TAG, "loginWithActivedUser");
        boolean isSuccessful = false;
        UserInfo activeUser = mUserInfoService.getLastLoginUserInfo(); //mUserManager.getActivedUser();
        if (activeUser != null) {
            String passwd = mDatabaseHelper.getUserPassword(activeUser); //mUserManager.getActiveUserPassword();
            if (!passwd.isEmpty()) {
                isSuccessful = login(activeUser.getUserPhoneName().trim(), passwd);
            }
        }
        return isSuccessful;
    }

    @Override
    public boolean login(String username, String password) throws RemoteException {
        boolean isSuccessful = false;
        isSuccessful = mUserInfoService.login(username, password);
        if(isSuccessful){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //MeasurementInfoService infoWrapper = new MeasurementInfoService(mContext, mDatabaseHelper);
                    //mMeasurementService.loadAllMeasurementsFromServer();
                    //mPushMessageService.loadPushmsgsFromServer(mDatabaseHelper.getActiveUserInfo().getUserId());
                }
            }).start();
        }
        return isSuccessful;
    }

    @Override
    public boolean logout() throws RemoteException {
        boolean isSuccessful = true;
        mDatabaseHelper.disableAllUserActived();
        return isSuccessful;
    }

    @Override
    public boolean registe(String username, String userpwd) throws RemoteException {
        boolean isSuccessful = false;
        isSuccessful = mUserInfoService.registe(username, userpwd);
        return isSuccessful;
    }

    @Override
    public boolean addUserExternInfo(String username, String email, String province,
                                     String city, String area, String address) throws RemoteException {
        boolean isSuccessful = false;
        isSuccessful = mUserInfoService.addUserExternInfo(username, email, province, city, area, address);
        return isSuccessful;

    }

    @Override
    public boolean updateUserInfo(UserInfo info) throws RemoteException {
        boolean isSuccessful = false;
        int updateRetCode = -1;
        updateRetCode = mUserInfoService.updateUserInfo(info);
        return isSuccessful;
    }

    @Override
    public boolean changePassword(String oldPassword, String keyCode, String newPassword) throws RemoteException {
        boolean isSuccessful = false;
        Log.d(TAG, "changePassword");
        int changePwdRetCode = -1;
        changePwdRetCode = mUserInfoService.changePassword(oldPassword, keyCode, newPassword);
        return isSuccessful;
    }

    public boolean changPasswdWithPhoneMsg(String phonenumber, String newPasswd, String confirmNewPasswd) throws RemoteException {
        boolean isSuccessful = false;
        int changePwdRetCode = -1;
        changePwdRetCode = mUserInfoService.changPasswdWithPhoneMsg(phonenumber, newPasswd, confirmNewPasswd);
        return isSuccessful;
    }



    @Override
    public boolean addMeasurement(String username,int deviceId, int deviceType, int delay) throws RemoteException {
        boolean isSuccessful = false;
        isSuccessful = mMeasurementService.addMeasurement(username, deviceId, deviceType, delay);
        return isSuccessful;
    }

    @Override
    public boolean delMeasurement(String username,int deviceId) throws RemoteException {
        boolean isSuccessful = false;
        isSuccessful = mMeasurementService.delMeasurement(username, deviceId);
        return isSuccessful;
    }

    @Override
    public boolean updateMeasurements(int uid) throws RemoteException {
        boolean isSuccessful = false;
        int ret = mMeasurementService.loadAllMeasurementsFromServer(uid);
        if(ret!=-1)
            isSuccessful=true;
        return isSuccessful;
    }

    @Override
    public boolean updateMeasurementRecords(int devId) throws RemoteException {
        boolean isSuccessful = false;
        Calendar c = Calendar.getInstance();
        String startTime = c.get(Calendar.YEAR) + "-" +
                (c.get(Calendar.MONTH) + 1) + "-" +
                (c.get(Calendar.DAY_OF_MONTH));

        c.add(Calendar.DAY_OF_YEAR, -4);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        String endTime = format.format(c.getTime());
        int ret = -1;
        ret = mMeasurementRecordService.loadRecordsFromServer(devId + "", startTime, endTime);
        return isSuccessful;

    }

    @Override
    public boolean fetchMeasurementTimedRecord(int devId, String startTime, String endTime) throws RemoteException {
        boolean isSuccessful = false;
        int ret = -1;
        ret = mMeasurementRecordService.loadRecordsFromServer(devId + "", startTime, endTime);
        return isSuccessful;
    }

    @Override
    public boolean fetchMeasurementList(int devId) throws RemoteException {
        boolean isSuccessful = false;
        int ret = -1;
        ret = mMeasurementListService.loadMestDatasFromServer(devId + "");
        return isSuccessful;
    }

    @Override
    public String checkDeviceState(int devId) throws RemoteException {
        boolean isSuccessful = false;
        String result = "";
        result = mMeasurementService.checkDeviceState(devId);
        return result;
    }

    @Override
    public boolean setDeviceState(int devId, int devState) throws RemoteException {
        boolean isSuccessful = false;
        String result = "";
        result = mMeasurementService.setDeviceState(devId, devState);
        return isSuccessful;
    }

    @Override
    public boolean getDeviceInfo(int devId) throws RemoteException {
        boolean isSuccessful = false;
        int ret = Constant.ErrorCode.GET_DEVICE_INFO_FAILED_CODE;
        ret = mMeasurementService.getDeviceInfo(devId);
        return isSuccessful;
    }

    @Override
    public boolean setDeviceConfig(int devId, Bundle params) throws RemoteException {
        boolean isSuccessful = false;
        int ret = -1;
        ret = mMeasurementService.setDeviceConfig(devId, params);
        if(ret==Constant.ErrorCode.DEV_SET_CONFIG_SUCCESSFULLY_CODE){
            isSuccessful=true;
        }
        return isSuccessful;
    }

    @Override
    public boolean setDeviceNotUseDay(int devId, Bundle params) throws RemoteException {
        boolean isSuccessful = false;
        int ret = -1;
        ret = mMeasurementService.setDeviceNotUseDay(devId, params);
        return isSuccessful;
    }

    @Override
    public boolean setEngneerDelay(int devId, Bundle params) throws RemoteException {
        boolean isSuccessful = false;
        int ret = -1;
        ret = mMeasurementService.setEngneerDelay(devId, params);
        return isSuccessful;
    }

    @Override
    public boolean setDeviceDateFlow(int devId, Bundle params) throws RemoteException {
        boolean isSuccessful = false;
        int ret = -1;
        ret = mMeasurementService.setDeviceDateFlow(devId, params);
        return isSuccessful;
    }

    @Override
    public boolean saveUpdelay(int devId, int delay) throws RemoteException {
        boolean isSuccessful = false;
        int saveUpdelayRetcode = -1;
        saveUpdelayRetcode = mMeasurementService.saveUpdelay(devId, delay);
        return isSuccessful;
    }

    @Override
    public boolean fetchProblemMessage(int uid) throws RemoteException {
        boolean isSuccessful = false;
        int ret = -1;
        ret = mProblemMessageService.loadProblemMsgServer(uid);
        return isSuccessful;
    }

    @Override
    public boolean fetchPushMessage(int uid) throws RemoteException {
        boolean isSuccessful = false;
        int ret = -1;
        ret = mPushMessageService.loadPushmsgsFromServer(uid);
        return isSuccessful;
    }

    @Override
    public boolean addPushMessage(int userId, String msg) throws RemoteException {
        return false;
    }

    @Override
    public boolean fetchSiglePushMessage(int pushid) throws RemoteException {
        boolean isSuccessful = false;
        int fetchSingleMessageRet = mPushMessageService.fetchSiglePushMessage(pushid);
        return isSuccessful;
    }

    @Override
    public boolean loadImageFromeServer(String imageUrl, String localImagePath) throws RemoteException {
        boolean isSuccessful = false;
        LoadDataFromServer.loadImageFromServer(imageUrl, localImagePath);
        return isSuccessful;
    }

    @Override
    public boolean getBitmap(int devId) throws RemoteException {
        return false;
    }

    @Override
    public boolean saveWifiInfomartion(AccessPointInfo info, String passwd) {
        boolean isSuccessful = false;
        mDatabaseHelper.saveApPasswd(info, passwd);
        return isSuccessful;
    }

    @Override
    public boolean setLocation() {
        boolean isSuccessful = false;
//        Map<String, String> parameters = new HashMap<>();
//        parameters.put("idend", mDatabaseHelper.getActiveUserInfo().getUserId() + "");
//        parameters.put("region", getLocation());
//        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_SET_LOCATION, parameters);
//        Future<JSONObject> connectionResult = mThreadPool.submit(task);
//        JSONObject data = null;
//        try {
//            data = connectionResult.get();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        JSONObject userInfoJsonObject = null;
//        if (data != null) {
//            try {
//                userInfoJsonObject = data.getJSONObject("UserInfo");
//                ret = userInfoJsonObject.getInt("status_code");
//            } catch (JSONException e) {
//                e.printStackTrace();
//                ret = ErrorNoMap.ERRNO_CANNOT_PARSER_JSON;
//                EasyLookApplication.DiagnoseProblem.diagnose(e, ret);
//            }
//        } else {
//            ret = ErrorNoMap.ERRNO_SERVER_RETURN_EMPTY;
//        }
        return isSuccessful;
    }

    public void getLocation() {

    }

    @Override
    public String getMessageVerifyCode(String phonenumber) throws RemoteException {
        String verifyCodeInfo = "";
        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put("phone", phonenumber+"");

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_MSG_VERIFY_CODE, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (data != null) {
            Log.d(TAG, "data = "+data);
            try {
                JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                int status_code = resultObject.getInt("status_code");
                String message = resultObject.getString("ret_message");
                if(status_code != ErrorCodeMap.ERRNO_GET_VERIFY_CODE_SUCCESSFULLY){
                    ExceptionCenter.process(mContext, new ErrorCode(status_code,message));
                } else {
                    StringBuffer sb = new StringBuffer();
                    int verifyCode = resultObject.getInt("msgcode");
                    int verifyCodeTimestamp = resultObject.getInt("time");
                    if(verifyCode != 0 && verifyCodeTimestamp != 0){
                        sb.append(verifyCode+","+verifyCodeTimestamp);
                        verifyCodeInfo = sb.toString();
                    }
                }
            } catch (JSONException e) {
                ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_PARSER_RESULT_JSON));
            }
        } else {
            ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_GET_RESULT_JSON));
        }
        return verifyCodeInfo;
    }

    @Override
    public boolean changPasswdWithPhoneMsg(String phonenumber, String newPasswd) throws RemoteException {
        boolean isSuccessful = false;
        int changePwdRetCode = -1;
        changePwdRetCode = mUserInfoService.changPasswdWithPhoneMsg(phonenumber, newPasswd, newPasswd);
        return changePwdRetCode>0?true:false;
    }


    public interface OnLocationChangedListener{
        void onLocationChanged(String province, String city, String township, String building, String street, String streetNumber);
    }
}
