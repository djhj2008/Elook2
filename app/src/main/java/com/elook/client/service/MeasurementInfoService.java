package com.elook.client.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.elook.client.ELookApplication;
import com.elook.client.exception.ErrorCode;
import com.elook.client.exception.ErrorCodeMap;
import com.elook.client.exception.ExceptionCenter;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by haiming on 5/20/16.
 */
public class MeasurementInfoService {
    private static final String TAG = "MeasurementInfoService";
    public static final String MEASUREMENT_TABLE = "device";
    public static final Uri MEASUREMENT_TABLE_URI = Uri.parse(ELookDatabaseHelper.BASE_URI+MEASUREMENT_TABLE);

    /*measurement table columns*/
    public static final String TABLE_COLUMN_DEVICEID = "device_device_id";
    public static final String TABLE_COLUMN_REG_TIME = "device_reg_time";
    public static final String TABLE_COLUMN_DEV_TYPE = "device_device_type";
    public static final String TABLE_COLUMN_PAY_ID = "device_pay_id";
    public static final String TABLE_COLUMN_BATTERY_LEVEL = "device_bettery_lev";
    public static final String TABLE_COLUMN_MOB_PIC_URL = "device_mob_url_pic";
    public static final String TABLE_COLUMN_DEV_PIC_URL = "device_dev_url_pic";
    public static final String TABLE_COLUMN_DEV_ERRPIC_URL = "device_dev_url_errpic";
    public static final String TABLE_COLUMN_DEV_CONFIG = "device_dev_config";
    public static final String TABLE_COLUMN_MOB_STATE = "device_mob_state";
    public static final String TABLE_COLUMN_DEV_ERROR = "device_dev_error";
    public static final String TABLE_COLUMN_DEV_SAVE_TIME = "device_dev_save_time";
    public static final String TABLE_COLUMN_UPL_STATE = "device_upl_state";
    public static final String TABLE_COLUMN_UP_DELAY = "device_up_delay";
    public static final String TABLE_COLUMN_UP_DELAY_SUB = "device_up_delay_sub";
    public static final String TABLE_COLUMN_DEV_STATE = "device_dev_state";
    public static final String TABLE_COLUMN_TMP_VALUE = "device_tmp_value";
    public static final String TABLE_COLUMN_SSID = "device_ssid";
    public static final String TABLE_COLUMN_SSID_PWD = "device_ssid_pwd";
    public static final String TABLE_COLUMN_DATE_INTERVAL = "device_date_interval";
    public static final String TABLE_COLUMN_DEV_FLOW = "device_device_flow";
    public static final String TABLE_COLUMN_ISACCESS = "device_dev_isaccess";
    public static final String TABLE_COLUMN_USER_PHONE_NAME = "device_user_phone_name";
    public static final String TABLE_COLUMN_CUR_VALUE = "device_cur_value";
    public static final String TABLE_COLUMN_SET_ACCESS = "device_set_access";
    public static final String TABLE_COLUMN_RECORD_DAY = "device_record_day";
    public static final String TABLE_COLUMN_ALISA = "device_alias";
    public static final String TABLE_COLUMN_LOCATION = "device_location";
    public static final String TABLE_COLUMN_YED_VALUE = "device_yed_value";
    public static final String TABLE_COLUMN_WEEK_VALUE = "device_week_value";
    public static final String TABLE_COLUMN_ID = "_id";

    public static final String CREATE_MEASUREMENTS_TABLE_SQL = "CREATE TABLE "+ MEASUREMENT_TABLE + " ("+
            TABLE_COLUMN_ID +" integer PRIMARY KEY autoincrement, "+
            TABLE_COLUMN_DEVICEID + " integer not null unique, " +
            TABLE_COLUMN_REG_TIME + " timestamp not null," +
            TABLE_COLUMN_DEV_TYPE +" tinyint default null, " +
            TABLE_COLUMN_PAY_ID +" text,"+
            TABLE_COLUMN_BATTERY_LEVEL + " tinyint, " +
            TABLE_COLUMN_MOB_PIC_URL + " text," +
            TABLE_COLUMN_DEV_PIC_URL + " text, " +
            TABLE_COLUMN_DEV_ERRPIC_URL + " text, " +
            TABLE_COLUMN_DEV_CONFIG + " text, " +
            TABLE_COLUMN_MOB_STATE + " tinyint, " +
            TABLE_COLUMN_DEV_ERROR + " integer, " +
            TABLE_COLUMN_DEV_SAVE_TIME + " timestamp" + "," +
            TABLE_COLUMN_UPL_STATE + " tinyint, " +
            TABLE_COLUMN_UP_DELAY + " integer, " +
            TABLE_COLUMN_UP_DELAY_SUB + " integer, " +
            TABLE_COLUMN_DEV_STATE + " tinyint," +
            TABLE_COLUMN_TMP_VALUE + " integer, " +
            TABLE_COLUMN_SSID + " text, " +
            TABLE_COLUMN_SSID_PWD + " text, " +
            TABLE_COLUMN_DATE_INTERVAL + " tinyint, " +
            TABLE_COLUMN_DEV_FLOW + " integer, " +
            TABLE_COLUMN_ISACCESS + " tinyint, "+
            TABLE_COLUMN_USER_PHONE_NAME + " text, "+
            TABLE_COLUMN_SET_ACCESS + " integer, " +
            TABLE_COLUMN_RECORD_DAY + " integer, " +
            TABLE_COLUMN_ALISA + " text, " +
            TABLE_COLUMN_LOCATION + " text, " +
            TABLE_COLUMN_CUR_VALUE + " integer, "+
            TABLE_COLUMN_YED_VALUE + " integer, "+
            TABLE_COLUMN_WEEK_VALUE + " integer"+
            ");";

    public static final String DEVICE_ID = "deviceid";
    public static final String DELAY = "delay";
    private static final int BATTERY_LEVEL_LOW = 0;
    private static final int BATTERY_LEVEL_NORMAL = 1;
    private static final int THREAD_POOL_SIZE = 5;

    ExecutorService mThreadPool;
    ELookDatabaseHelper mDatabaseHelper;
//    UserInfo mCurrentUser;
    private SQLiteDatabase mReadableDatabase, mWritableDatabase;
    private Context mContext;

    public MeasurementInfoService(Context c,ELookDatabaseHelper databaseHelper){
        mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mDatabaseHelper = databaseHelper;
        this.mContext = c;
    }

    private int isMeasurementExisted(int devId){
        int keyId = -1;
        if(devId <= 0){
            Log.e(TAG, "Device Id is empty. Cannot query");
            return keyId;
        }
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        Cursor cursor = mReadableDatabase.query(true, MEASUREMENT_TABLE, new String[]{TABLE_COLUMN_ID, TABLE_COLUMN_DEVICEID},
                null, null, null, null, null, null);

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                if (devId == cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEVICEID))){
                    keyId = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_ID));
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        return keyId;
    }

    private int insertMeasurementInfo(SQLiteDatabase db, MeasurementInfo info){
        int ret = -1;
        int keyId = isMeasurementExisted(info.getDeviceId());
        boolean isExisted = keyId > 0 ? true: false;


        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMN_DEVICEID, info.getDeviceId());
        contentValues.put(TABLE_COLUMN_REG_TIME, info.getRegTimestamp());
        contentValues.put(TABLE_COLUMN_DEV_STATE, info.getDeviceState());

        contentValues.put(TABLE_COLUMN_DEV_TYPE, info.getDeviceType());
        contentValues.put(TABLE_COLUMN_PAY_ID, info.getPayId());
        contentValues.put(TABLE_COLUMN_BATTERY_LEVEL, info.getDeviceBatteryLevel());
        if(info.getMobUrlPic() != null && !info.getMobUrlPic().isEmpty()) contentValues.put(TABLE_COLUMN_MOB_PIC_URL, info.getMobUrlPic());
        if(info.getDeviceUrlPic() != null && !info.getDeviceUrlPic().isEmpty()) contentValues.put(TABLE_COLUMN_DEV_PIC_URL, info.getDeviceUrlPic());
        if(info.getDeviceUrlErrorPic() != null && !info.getDeviceUrlErrorPic().isEmpty()) contentValues.put(TABLE_COLUMN_DEV_ERRPIC_URL, info.getDeviceUrlErrorPic());
        if(info.getConfig() != null && !info.getConfig().isEmpty()) contentValues.put(TABLE_COLUMN_DEV_CONFIG, info.getConfig());
        contentValues.put(TABLE_COLUMN_MOB_STATE, info.getMobState());
        contentValues.put(TABLE_COLUMN_DEV_ERROR, info.getError());
        contentValues.put(TABLE_COLUMN_DEV_SAVE_TIME, info.getSaveTimestamp());
        contentValues.put(TABLE_COLUMN_UPL_STATE, info.getUplState());
        contentValues.put(TABLE_COLUMN_UP_DELAY, info.getUpdelay());
        contentValues.put(TABLE_COLUMN_UP_DELAY_SUB, info.getmUpdelaySub());
        contentValues.put(TABLE_COLUMN_TMP_VALUE, info.getTmpValue());
        if(info.getSSID() != null && !info.getSSID().isEmpty()) contentValues.put(TABLE_COLUMN_SSID, info.getSSID());
        if(info.getWordpass() != null && !info.getWordpass().isEmpty()) contentValues.put(TABLE_COLUMN_SSID_PWD, info.getWordpass());
        contentValues.put(TABLE_COLUMN_DATE_INTERVAL, info.getDateInterval());
        contentValues.put(TABLE_COLUMN_DEV_FLOW, info.getDeviceFlow());
        contentValues.put(TABLE_COLUMN_ISACCESS, info.getIsAccess());
        contentValues.put(TABLE_COLUMN_USER_PHONE_NAME, info.getUserPhoneName());
        contentValues.put(TABLE_COLUMN_CUR_VALUE, info.getCurValue());
        contentValues.put(TABLE_COLUMN_SET_ACCESS, info.getDevNotUseDay());
        contentValues.put(TABLE_COLUMN_RECORD_DAY, info.getDevNotUseDayCount());
        contentValues.put(TABLE_COLUMN_ALISA, info.getAlisaName());
        contentValues.put(TABLE_COLUMN_LOCATION, info.getLocation());
        contentValues.put(TABLE_COLUMN_YED_VALUE, info.getYedValue());
        contentValues.put(TABLE_COLUMN_WEEK_VALUE, info.getWeekValue());
        if(isExisted){
            String where = TABLE_COLUMN_ID + " = " + keyId;
            ret = db.update(MEASUREMENT_TABLE, contentValues, where, null);
        } else {
            ret = (int)db.insert(MEASUREMENT_TABLE, null, contentValues);
        }
        return ret;
    }

    private int insertMeasurementConfig(SQLiteDatabase db, MeasurementInfo info){
        int ret = -1;
        int keyId = isMeasurementExisted(info.getDeviceId());
        boolean isExisted = keyId > 0 ? true: false;
        Log.d(TAG, "insertMeasurementConfig:payid="+info.getPayId());
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMN_PAY_ID, info.getPayId());
        contentValues.put(TABLE_COLUMN_BATTERY_LEVEL, info.getDeviceBatteryLevel());
        contentValues.put(TABLE_COLUMN_MOB_STATE, info.getMobState());
        contentValues.put(TABLE_COLUMN_UPL_STATE, info.getUplState());
        contentValues.put(TABLE_COLUMN_UP_DELAY, info.getUpdelay());
        contentValues.put(TABLE_COLUMN_ALISA, info.getAlisaName());
        contentValues.put(TABLE_COLUMN_LOCATION, info.getLocation());
        contentValues.put(TABLE_COLUMN_DEV_STATE, info.getDeviceState());
        if(isExisted){
            String where = TABLE_COLUMN_ID + " = " + keyId;
            ret = db.update(MEASUREMENT_TABLE, contentValues, where, null);
        }
        return ret;
    }

    private int insertMeasurementInfoOther(SQLiteDatabase db, MeasurementInfo info){
        int ret = -1;
        int keyId = isMeasurementExisted(info.getDeviceId());
        boolean isExisted = keyId > 0 ? true: false;


        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMN_DEVICEID, info.getDeviceId());
        contentValues.put(TABLE_COLUMN_REG_TIME, info.getRegTimestamp());
        contentValues.put(TABLE_COLUMN_DEV_STATE, info.getDeviceState());

        contentValues.put(TABLE_COLUMN_DEV_TYPE, info.getDeviceType());
        contentValues.put(TABLE_COLUMN_PAY_ID, info.getPayId());
        contentValues.put(TABLE_COLUMN_BATTERY_LEVEL, info.getDeviceBatteryLevel());
        if(info.getMobUrlPic() != null && !info.getMobUrlPic().isEmpty()) contentValues.put(TABLE_COLUMN_MOB_PIC_URL, info.getMobUrlPic());
        if(info.getDeviceUrlPic() != null && !info.getDeviceUrlPic().isEmpty()) contentValues.put(TABLE_COLUMN_DEV_PIC_URL, info.getDeviceUrlPic());
        if(info.getDeviceUrlErrorPic() != null && !info.getDeviceUrlErrorPic().isEmpty()) contentValues.put(TABLE_COLUMN_DEV_ERRPIC_URL, info.getDeviceUrlErrorPic());
        if(info.getConfig() != null && !info.getConfig().isEmpty()) contentValues.put(TABLE_COLUMN_DEV_CONFIG, info.getConfig());
        contentValues.put(TABLE_COLUMN_MOB_STATE, info.getMobState());
        contentValues.put(TABLE_COLUMN_DEV_ERROR, info.getError());
        contentValues.put(TABLE_COLUMN_DEV_SAVE_TIME, info.getSaveTimestamp());
        contentValues.put(TABLE_COLUMN_UPL_STATE, info.getUplState());
        contentValues.put(TABLE_COLUMN_UP_DELAY, info.getUpdelay());
        contentValues.put(TABLE_COLUMN_UP_DELAY_SUB, info.getmUpdelaySub());
        contentValues.put(TABLE_COLUMN_TMP_VALUE, info.getTmpValue());
        if(info.getSSID() != null && !info.getSSID().isEmpty()) contentValues.put(TABLE_COLUMN_SSID, info.getSSID());
        if(info.getWordpass() != null && !info.getWordpass().isEmpty()) contentValues.put(TABLE_COLUMN_SSID_PWD, info.getWordpass());
        contentValues.put(TABLE_COLUMN_DATE_INTERVAL, info.getDateInterval());
        contentValues.put(TABLE_COLUMN_DEV_FLOW, info.getDeviceFlow());
        contentValues.put(TABLE_COLUMN_ISACCESS, info.getIsAccess());
        contentValues.put(TABLE_COLUMN_SET_ACCESS, info.getDevNotUseDay());
        contentValues.put(TABLE_COLUMN_RECORD_DAY, info.getDevNotUseDayCount());
        contentValues.put(TABLE_COLUMN_ALISA, info.getAlisaName());
        contentValues.put(TABLE_COLUMN_LOCATION, info.getLocation());
        contentValues.put(TABLE_COLUMN_YED_VALUE, info.getYedValue());
        contentValues.put(TABLE_COLUMN_WEEK_VALUE, info.getWeekValue());
        if(isExisted){
            String where = TABLE_COLUMN_ID + " = " + keyId;
            ret = db.update(MEASUREMENT_TABLE, contentValues, where, null);
        } else {
            ret = (int)db.insert(MEASUREMENT_TABLE, null, contentValues);
        }
        return ret;
    }

    public void cleanMeasurementInfos(){
        Log.d(TAG, "cleanMeasurementInfos");
        if(mWritableDatabase == null)mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        mWritableDatabase.execSQL("delete from "+MEASUREMENT_TABLE);
        mWritableDatabase.beginTransaction();
        mWritableDatabase.setTransactionSuccessful();
        mWritableDatabase.endTransaction();
    }

    public void saveMeasurementInfos(List<MeasurementInfo> infos){
        Log.d(TAG, "saveMeasurementInfos");
        int affectCount = 0;
        if(mWritableDatabase == null)mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        mWritableDatabase.execSQL("delete from "+MEASUREMENT_TABLE);
        mWritableDatabase.beginTransaction();
        for (MeasurementInfo info : infos) {
            affectCount += insertMeasurementInfo(mWritableDatabase, info);
        }
        mWritableDatabase.setTransactionSuccessful();
        mWritableDatabase.endTransaction();
        if(affectCount > 0){
            mContext.getContentResolver().notifyChange(MEASUREMENT_TABLE_URI,null);
        }
    }

    public boolean saveMeasurementInfo(MeasurementInfo info){
        boolean successfullySaved = false;
        if(mWritableDatabase == null)mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        if(insertMeasurementInfo(mWritableDatabase, info) > 0) {
            successfullySaved = true;
            mContext.getContentResolver().notifyChange(MEASUREMENT_TABLE_URI, null);
        }

        return successfullySaved;
    }

    public boolean saveMeasurementConfig(MeasurementInfo info){
        boolean successfullySaved = false;
        if(mWritableDatabase == null)mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        if(insertMeasurementConfig(mWritableDatabase, info) > 0) {
            successfullySaved = true;
            mContext.getContentResolver().notifyChange(MEASUREMENT_TABLE_URI, null);
        }

        return successfullySaved;
    }

    public boolean saveMeasurementInfoOther(MeasurementInfo info){
        boolean successfullySaved = false;
        if(mWritableDatabase == null)mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        if(insertMeasurementInfoOther(mWritableDatabase, info) > 0) {
            successfullySaved = true;
            mContext.getContentResolver().notifyChange(MEASUREMENT_TABLE_URI, null);
        }

        return successfullySaved;
    }

    private MeasurementInfo getMeasurementInfoFromCursor(Cursor cursor) {
        if(cursor == null){
            return null;
        }
        MeasurementInfo info = null;
        int deviceId = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEVICEID));
        long time = cursor.getLong(cursor.getColumnIndex(TABLE_COLUMN_REG_TIME));
        int state = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_STATE));
        info = new MeasurementInfo(deviceId, time, state);
        info.setPayId(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_PAY_ID)));
        info.setDeviceBatteryLevel(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_BATTERY_LEVEL)));
        info.setMobState(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_MOB_STATE)));
        info.setError(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_ERROR)));
        info.setSaveTimestamp(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_SAVE_TIME)));
        info.setUplState(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_UPL_STATE)));
        info.setUpdelay(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_UP_DELAY)));
        info.setUpdelaySub(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_UP_DELAY_SUB)));
        info.setTmpValue(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_TMP_VALUE)));
        info.setDateInterval(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DATE_INTERVAL)));
        info.setDeviceFlow(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_FLOW)));
        info.setIsAccess(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_ISACCESS)));
        info.setUserPhoneName(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_USER_PHONE_NAME)));
        info.setDeviceType(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_TYPE)));
        info.setCurValue(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_CUR_VALUE)));
        info.setDevNotUseDay(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_SET_ACCESS)));
        info.setDevNotUseDayCount(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_RECORD_DAY)));
        info.setYedValue(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_YED_VALUE)));
        info.setWeekValue(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_WEEK_VALUE)));

        String ssid = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_SSID));
        if (ssid != null && !ssid.isEmpty()) info.setSSID(ssid);

        String wordpass = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_SSID_PWD));
        if (wordpass != null && !wordpass.isEmpty()) info.setWordpass(wordpass);

        String moburlpic = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_MOB_PIC_URL));
        if (moburlpic != null && !moburlpic.isEmpty()) info.setMobUrlPic(moburlpic);

        String deviceurlpic = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_DEV_PIC_URL));
        if (deviceurlpic != null && !deviceurlpic.isEmpty()) info.setDeviceUrlPic(deviceurlpic);

        String deviceurlerrorpic = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_DEV_ERRPIC_URL));
        if (deviceurlerrorpic != null && !deviceurlerrorpic.isEmpty()) info.setDeviceUrlErrorPic(deviceurlerrorpic);

        String alisaname = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_ALISA));
        if (alisaname != null && !alisaname.isEmpty()) info.setAlisaName(alisaname);

        String location = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_LOCATION));
        if (location != null && !location.isEmpty()) info.setLocation(location);

        String config = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_DEV_CONFIG));
        if (config != null && !config.isEmpty()) info.setConfig(config);

        if (info != null) info.setRecords(mDatabaseHelper.getRecords(deviceId));
        return info;
    }

    public MeasurementInfo getMeasurementInfo(int devId){
        MeasurementInfo info = null;
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }
        Cursor cursor = mReadableDatabase.query(true, MEASUREMENT_TABLE, null,
                TABLE_COLUMN_DEVICEID +"=?", new String[]{devId+""}, null, null, null, null);

        if(cursor.getCount() == 1){
            cursor.moveToFirst();
            info = getMeasurementInfoFromCursor(cursor);
        }
        cursor.close();
        return info;
    }

    public List<MeasurementInfo> getMeasurementsOfUser(String userphonename){
        List<MeasurementInfo> measurements = new ArrayList<>();
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }
//        loadAllMeasurementsFromServer();
        Cursor cursor = mReadableDatabase.query(true, MEASUREMENT_TABLE, null,
                TABLE_COLUMN_USER_PHONE_NAME + "=?", new String[]{userphonename + ""}, null, null, null, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                MeasurementInfo info = getMeasurementInfoFromCursor(cursor);
                if(info != null) measurements.add(info);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return measurements;
    }



    public int getMeasurementTypeWithDevTy(int devId) {
        String tempDevId = devId + "";
        char firstChar = tempDevId.charAt(1);
        int tempDevty = Character.digit(firstChar, 10);
        return tempDevty;
    }

    public boolean delMeasurement(String username,int deviceId){
        int status_code = -1;
        boolean isSuccessful = false;
        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put(ELookServiceImpl.HTTP_PARAMS_ENDUSERNAME, username);
        parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_ID, deviceId+"");

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_DEL_DEVICE, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (data != null) {
            try {
                JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                status_code = resultObject.getInt("status_code");
                String message = resultObject.getString("ret_message");
                if(status_code != ErrorCodeMap.ERRNO_DEL_DEVICE_SUCCESSFULLY
                         &&status_code != ErrorCodeMap.ERRNO_DEL_DEVICE_SUCCESSFULLY2){
                    ExceptionCenter.process(mContext, new ErrorCode(status_code, message));
                } else {
                    isSuccessful = true;
                }
            } catch (JSONException e) {
                ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_PARSER_RESULT_JSON));
                e.printStackTrace();
            }
        } else {
            ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_GET_RESULT_JSON));
        }
        return isSuccessful;
    }


    public boolean addMeasurement(String username,int deviceId, int deviceType, int delay) {
        int status_code = -1;
        boolean isSuccessful = false;
        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put(ELookServiceImpl.HTTP_PARAMS_ENDUSERNAME, username);
        parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_ID, deviceId+"");
        parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_DELAY, delay + "");
        parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_TYPE, deviceType + "");

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_ADD_DEVICE, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (data != null) {
            try {
                JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                status_code = resultObject.getInt("status_code");
                String message = resultObject.getString("ret_message");
                if(status_code != ErrorCodeMap.ERRNO_ADD_DEVICE_SUCCESSFULLY){
                    ExceptionCenter.process(mContext, new ErrorCode(status_code, message));
                } else {
                    isSuccessful = true;
                    JSONObject measurementJsonObject = resultObject.getJSONObject("data");
                    MeasurementInfo info = new MeasurementInfo(measurementJsonObject);
                    info.setUserPhoneName(mDatabaseHelper.getActiveUserInfo().getUserPhoneName());
                    info.setCurValue(0);
                    mDatabaseHelper.saveMeasurementInfo(info);
                }
            } catch (JSONException e) {
                ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_PARSER_RESULT_JSON));
                e.printStackTrace();
            }
        } else {
            ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_GET_RESULT_JSON));
        }
        return isSuccessful;
    }

    private JSONObject loadContentPageFromServer(String url, Map<String, String> parameters) {
        LoadDataFromServerTask task = new LoadDataFromServerTask(url, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject result = null;
        try {
            result = connectionResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return result;
    }

    private int getAllPageNumbers(JSONObject data, boolean isRecord) {
        int allPageNumbers = -1;
        String ret = "";
        try {
            JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
            int status_code = resultObject.getInt("status_code");
            if (status_code != ErrorCodeMap.ERRNO_CHECKIN_DEVICE_SUCCESSFULLY) {
                Log.e(TAG, "Cannot get correct data");
                return -1;
            }
            allPageNumbers = Integer.parseInt(resultObject.getString("pagenum"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allPageNumbers;
    }

    private int parserContentPageResult(JSONObject data, boolean isRecord) {
        int curPageNum = -1;
        int status_code = -1;
        String ret = "";
        try {
            JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
            status_code = resultObject.getInt("status_code");
            String message = resultObject.getString("ret_message");
            if(status_code != ErrorCodeMap.ERRNO_CHECKIN_DEVICE_SUCCESSFULLY){
                //ExceptionCenter.process(mContext, new ErrorCode(status_code, message));
            } else {
                curPageNum = Integer.parseInt(resultObject.getString("page"));
                JSONArray jsonArray = resultObject.getJSONArray("infoone");
                JSONArray jsonRecordArray = resultObject.getJSONArray("infotwo");
                saveMeasurementsFromPage(jsonArray, jsonRecordArray);

            }

        } catch (JSONException e) {
            ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_PARSER_RESULT_JSON));
        }
        return curPageNum;
    }

    public int loadAllMeasurementsFromServer(int uid) {
        int curPageNum = -1;
        int allPageNumbers = -1;
        int ret = -1;
        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put(ELookServiceImpl.HTTP_PARAMS_ENDUSERID, mDatabaseHelper.getActiveUserInfo().getUserId() + "");
        parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_PAGE, 1 + "");

        /*At least 1 page.*/
        JSONObject result = loadContentPageFromServer(Constant.URL_CHECKIN_DEV, parameters);

        Log.d(TAG,"loadAllMeasurementsFromServer result:"+result);
        allPageNumbers = getAllPageNumbers(result, false);
        ret = curPageNum = parserContentPageResult(result, false);
        if(allPageNumbers == -1){
            mDatabaseHelper.cleanMeasurementInfos();
        }
        return ret;
    }

    private void saveMeasurementsFromPage(JSONArray infooneJsonObject,JSONArray infotwo) {
        List<MeasurementInfo> measurementInfos = new ArrayList<>();
        Log.d(TAG, "saveMeasurementsFromPage, real count = "+infooneJsonObject.length());
        try {
            for (int i = 0; i < infooneJsonObject.length(); i++) {
                JSONObject measurementJsonObject =
                        infooneJsonObject.getJSONObject(i);
                MeasurementInfo info = new MeasurementInfo(measurementJsonObject);
                info.setUserPhoneName(mDatabaseHelper.getActiveUserInfo().getUserPhoneName());
                info.setCurValue(getMeasurementsRecord(infotwo, info.getDeviceId()));
                info.setYedValue(getMeasurementsYedValue(infotwo, info.getDeviceId()));
                info.setWeekValue(getMeasurementsWeekValue(infotwo, info.getDeviceId()));
                measurementInfos.add(info);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mDatabaseHelper.saveMeasurementInfos(measurementInfos);
    }

    private int getMeasurementsRecord(JSONArray infotwo,int mDevId){
        int curValue = -1;
        try {
            for (int i = 0; i < infotwo.length(); i++) {
                JSONObject measurementJsonObject =
                        infotwo.getJSONObject(i);
                int devid = measurementJsonObject.getInt("access_device_id");
                if(devid == mDevId){
                    curValue = measurementJsonObject.getInt("access_value");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return curValue;
    }

    private boolean isAvaliableInteger(String integerStr){
        boolean isAvaliable = true;
        if(integerStr.length() <= 0){
            return false;
        }
        for (int i = 0; i < integerStr.length(); i++){
            if(!Character.isDigit(integerStr.charAt(i))){
                isAvaliable = false;
                break;
            }
        }
        return isAvaliable;
    }

    private int getMeasurementsYedValue(JSONArray infotwo,int mDevId){
        int curValue = -1;
        try {
            for (int i = 0; i < infotwo.length(); i++) {
                JSONObject measurementJsonObject =
                        infotwo.getJSONObject(i);
                int devid = measurementJsonObject.getInt("access_device_id");
                if(devid == mDevId){
                    String str = measurementJsonObject.getString("yesterday");
                    if(isAvaliableInteger(str)){
                        curValue = Integer.parseInt(str);
                    }else {
                        curValue = 0;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return curValue;
    }

    private int getMeasurementsWeekValue(JSONArray infotwo,int mDevId){
        int curValue = -1;
        try {
            for (int i = 0; i < infotwo.length(); i++) {
                JSONObject measurementJsonObject =
                        infotwo.getJSONObject(i);
                int devid = measurementJsonObject.getInt("access_device_id");
                if(devid == mDevId){
                    String str = measurementJsonObject.getString("weekvalue");
                    if(isAvaliableInteger(str)){
                        curValue = Integer.parseInt(str);
                    }else {
                        curValue = 0;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return curValue;
    }

    public int getDeviceInfo(int devId) {
        int ret = Constant.ErrorCode.GET_DEVICE_INFO_FAILED_CODE;
        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put("deviceid", devId + "");

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_GET_DEV_INFO  , parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        if (data != null) {
            try {
                JSONObject userInfo = data.getJSONObject("UserInfo");
                String resultString = userInfo.getString("ret");
                Log.d(TAG, "ret: " + resultString);
                if (resultString.trim().equals(Constant.ErrorCode.GET_DEVICE_INFO_SUCCESSFULLY_MESSAGE)) {
                    ret = Constant.ErrorCode.GET_DEVICE_INFO_SUCCESSFULLY_CODE;
                    JSONObject measurementJsonObject = userInfo.getJSONObject("info");
                    MeasurementInfo info = new MeasurementInfo(measurementJsonObject);
                    mDatabaseHelper.saveMeasurementInfoOther(info);
                }

            } catch (JSONException e) {
                ret = Constant.ErrorCode.GET_DEVICE_INFO_FAILED_CODE;
                e.printStackTrace();
            }
        } else {
            ret = Constant.ErrorCode.GET_DEVICE_INFO_FAILED_CODE;
        }
        return ret;
    }

    public int setDeviceDateFlow(int devId, Bundle params) {
        int ret = -1;
        HashMap<String, String> parameters = new HashMap<>();
        for (String key : params.keySet()){
            String value = params.getString(key);
            parameters.put(key, value);
        }

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_DATE_FLOW, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (data != null) {
            try {
                JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                int status_code = resultObject.getInt("status_code");
                if(status_code == ErrorCodeMap.ERRNO_DATAFLOW_SUCCESSFULLY ) {
                    {
                        ret = Constant.ErrorCode.SET_DATE_FLOW_SUCCESSFULLY_CODE;
                        JSONObject measurementJsonObject = resultObject.getJSONObject("data");
                        MeasurementInfo info = new MeasurementInfo(measurementJsonObject);
                        mDatabaseHelper.saveMeasurementInfoOther(info);
                    }
                }
            } catch (JSONException e) {
                ret = Constant.ErrorCode.SET_DATE_FLOW_FAILED_CODE;
                e.printStackTrace();
            }
        } else {
            ret = Constant.ErrorCode.SET_DATE_FLOW_FAILED_CODE;
        }
        return ret;
    }

    public int setDeviceConfig(int devId, Bundle params) {
        int ret = -1;
        HashMap<String, String> parameters = new HashMap<>();
        for (String key : params.keySet()){
            String value = params.getString(key);
            parameters.put(key, value);
        }
        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_DEV_SET_CONFIG, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (data != null) {
            try {
                JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                int status_code = resultObject.getInt("status_code");
                Log.d(TAG,"setDeviceConfig status_code:"+status_code);
                if(status_code == ErrorCodeMap.ERRNO_DATAFLOW_SUCCESSFULLY ) {
                    {
                        Log.d(TAG,"setDeviceConfig success!");
                        ret = Constant.ErrorCode.DEV_SET_CONFIG_SUCCESSFULLY_CODE;
                        JSONObject measurementJsonObject = resultObject.getJSONObject("data");
                        MeasurementInfo info = new MeasurementInfo(measurementJsonObject);
                        mDatabaseHelper.saveMeasurementConfig(info);
                    }
                }else{
                    ret = Constant.ErrorCode.DEV_SET_CONFIG_FAILED_CODE;
                }
            } catch (JSONException e) {
                ret = Constant.ErrorCode.DEV_SET_CONFIG_FAILED_CODE;
                e.printStackTrace();
            }
        } else {
            ret = Constant.ErrorCode.DEV_SET_CONFIG_FAILED_CODE;
        }
        return ret;
    }

    public int setDeviceNotUseDay(int devId, Bundle params) {
        int ret = -1;
        HashMap<String, String> parameters = new HashMap<>();
        for (String key : params.keySet()){
            String value = params.getString(key);
            parameters.put(key, value);
        }

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_SET_NOT_USE_DAY, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (data != null) {
            try {
                JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                int status_code = resultObject.getInt("status_code");
                if(status_code == ErrorCodeMap.ERRNO_NOTUSE_DAY_SUCCESSFULLY ) {
                    {
                        ret = Constant.ErrorCode.SAVE_NOTUSE_DAY_SUCCESSFULLY_CODE;
                        JSONObject measurementJsonObject = resultObject.getJSONObject("data");
                        MeasurementInfo info = new MeasurementInfo(measurementJsonObject);
                        mDatabaseHelper.saveMeasurementInfoOther(info);
                    }
                }
            } catch (JSONException e) {
                ret = Constant.ErrorCode.SAVE_NOTUSE_DAY_FAILED_CODE;
                e.printStackTrace();
            }
        } else {
            ret = Constant.ErrorCode.SAVE_NOTUSE_DAY_FAILED_CODE;
        }
        return ret;
    }


    public int setEngneerDelay(int devId, Bundle params) {
        int ret = -1;
        HashMap<String, String> parameters = new HashMap<>();
        for (String key : params.keySet()){
            String value = params.getString(key);
            parameters.put(key, value);
        }

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_SET_ENGNEER_DELAY, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (data != null) {
            try {
                JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                int status_code = resultObject.getInt("status_code");
                if(status_code == ErrorCodeMap.ERRNO_NOTUSE_DAY_SUCCESSFULLY ) {
                    {
                        ret = Constant.ErrorCode.SAVE_NOTUSE_DAY_SUCCESSFULLY_CODE;
                        JSONObject measurementJsonObject = resultObject.getJSONObject("data");
                        MeasurementInfo info = new MeasurementInfo(measurementJsonObject);
                        mDatabaseHelper.saveMeasurementInfoOther(info);
                    }
                }
            } catch (JSONException e) {
                ret = Constant.ErrorCode.SAVE_NOTUSE_DAY_FAILED_CODE;
                e.printStackTrace();
            }
        } else {
            ret = Constant.ErrorCode.SAVE_NOTUSE_DAY_FAILED_CODE;
        }
        return ret;
    }

    public int saveUpdelay(int devId, int delay){
        int saveUpdelayRetcode = -1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(DEVICE_ID, devId + "");
        parameters.put(DELAY, "" + delay);
        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_SET_DELAY, parameters);
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
            try {
                JSONObject userInfo = data.getJSONObject("UserInfo");
                String resultString = userInfo.getString("ret");
                Log.d(TAG, "ret: " + resultString);
                if (resultString.trim().equals(Constant.ErrorCode.SAVE_UPDELAY_SUCCESSFULLY_MESSAGE))
                    saveUpdelayRetcode = Constant.ErrorCode.SAVE_UPDELAY_SUCCESSFULLY_CODE;

            } catch (JSONException e) {
                saveUpdelayRetcode = -1;
                e.printStackTrace();
            }
        } else {
            saveUpdelayRetcode = -1;
        }
        return saveUpdelayRetcode;
    }

    public String setDeviceState(int devId, int devState) {
        String parameters = devId + "" + BATTERY_LEVEL_NORMAL + ((byte) (devState & 0xFF));

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_SET_DEV_STATE, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        if (data != null) {
            try {
                result = data.getString("result");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String checkDeviceState(int devId) {
        String parameters = devId + "" + BATTERY_LEVEL_NORMAL;

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_CHECK_DEV_STATE, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String result = "";
        Log.d(TAG, "data = " + data);
        if (data != null) {
            try {
                result = data.getString("result");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
