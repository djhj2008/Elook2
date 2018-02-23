package com.elook.client.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.elook.client.exception.ErrorCodeMap;
import com.elook.client.user.MeasurementCountData;
import com.elook.client.user.MeasurementRecord;
import com.elook.client.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
public class MeasurementListService {
    private static final String TAG = "MeasurementList";
    public static final String MEASUREMENT_LIST_TABLE = "measurement_list";
    public static final Uri MEASUREMENT_LIST_TABLE_URI = Uri.parse(ELookDatabaseHelper.BASE_URI+MEASUREMENT_LIST_TABLE);

    /*
{"7dayvalue":
[{"day":1466930645,"value":null},
{"day":1466784000,"value":null},
{"day":1466697600,"value":null},
{"day":1466611200,"value":null},
{"day":1466524800,"value":null},
{"day":1466438400,"value":null},
{"day":1466352000,"value":null}],
"7weekvalue":
[{"day":1466930645,"value":null},
{"day":1466352000,"value":null},
{"day":1465747200,"value":null},
{"day":1465142400,"value":null},
{"day":1464537600,"value":null},
{"day":1463932800,"value":null},
{"day":1463328000,"value":null}],
"7monthvalue":
[{"day":1466930645,"value":null},
{"day":1462032000,"value":null},
{"day":1459440000,"value":null},
{"day":1456761600,"value":null},
{"day":1454256000,"value":null},
{"day":1451577600,"value":null},
{"day":1448899200,"value":null}],
"3yearvalue":
[{"day":1466930645,"value":null},
{"day":1420041600,"value":null},
{"day":1388505600,"value":null}]}
*/
    public static final String TABLE_COLUMN_ID = "mesm_autoid";
    public static final String TABLE_COLUMN_DEV_ID = "mesm_device_id";
    public static final String TABLE_COLUMN_DEV_VALUE = "mesm_value";
    public static final String TABLE_COLUMN_TIMESTAMP = "mesm_time";
    public static final String TABLE_COLUMN_TYPE = "mesm_type";

    public static final int TABLE_TYPE_DAY = 1;
    public static final int TABLE_TYPE_WEEK = 2;
    public static final int TABLE_TYPE_MONTH = 3;
    public static final int TABLE_TYPE_YEAR= 4;

    public static final String CREATE_MEASUREMENT_LIST_SQL = "CREATE TABLE "+ MEASUREMENT_LIST_TABLE + " ("+
            TABLE_COLUMN_ID +" integer not null PRIMARY KEY AUTOINCREMENT, "+
            TABLE_COLUMN_DEV_ID + " integer not null, "+
            TABLE_COLUMN_DEV_VALUE + " text not null, " +
            TABLE_COLUMN_TIMESTAMP + " integer not null, "+
            TABLE_COLUMN_TYPE + " tinyint" +
            ");";

    private static final int THREAD_POOL_SIZE = 5;

    ExecutorService mThreadPool;
    ELookDatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mReadableDatabase, mWritableDatabase;
    private Context mContext;

    public MeasurementListService(Context c, ELookDatabaseHelper databaseHelper){
        mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mDatabaseHelper = databaseHelper;
        this.mContext = c;
    }

    public List<MeasurementCountData> getMeasurementDayDatas(int devId,int type) {
        int value = 0;
        int datetime;
        int devid;
        List<MeasurementCountData> MemsCoutDatas = new ArrayList<>();
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }
        String SQL = "select distinct * from "+MEASUREMENT_LIST_TABLE+" where " + TABLE_COLUMN_DEV_ID+" = "+devId+ " and "+TABLE_COLUMN_TYPE+" = "+ type +";";
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                devid = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_ID));
                value = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_VALUE));
                datetime = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_TIMESTAMP));
                int recordId = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_ID));
                int mtype = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_TYPE));
                MeasurementCountData data = new MeasurementCountData(recordId,devid, datetime, value,mtype);
                MemsCoutDatas.add(data);
            }while (cursor.moveToNext());
        } else {

        }
        cursor.close();
        return MemsCoutDatas;
    }

    private int isRecordExisted(MeasurementCountData data){
        if(data == null){
            Log.e(TAG, "Record is empty. Cannot query");
            return -1;
        }
        boolean isExisted = false;
        int recordDate = data.getDateTime();

        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        /*select * from MEASUREMENT_RECORDS_TABLE where RECORD_TIMESTAMP = recordate;  */
        final String SQL = "select distinct  * from "+ MEASUREMENT_LIST_TABLE +
                " where "+ TABLE_COLUMN_ID +" = "+ data.getRecordId()+ ";";
        int recordId = -1;
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);
        if (cursor.getCount() > 0)  {
            recordId = data.getRecordId();
        }else {
            recordId = -1;
        }
        cursor.close();
        return  recordId;
    }

    private int insertRecord(SQLiteDatabase db, MeasurementCountData data){
        int keyId = isRecordExisted(data);
        boolean isExisted = keyId > 0 ? true: false;

        ContentValues contentValues = new ContentValues();
        //contentValues.put(TABLE_COLUMN_ID, data.getRecordId());
        contentValues.put(TABLE_COLUMN_DEV_ID, data.getDevId());
        contentValues.put(TABLE_COLUMN_DEV_VALUE, data.getRecordValue());
        contentValues.put(TABLE_COLUMN_TIMESTAMP, data.getDateTime());
        contentValues.put(TABLE_COLUMN_TYPE, data.getType());

        int ret = 0;
        //if(isExisted){
        //String where = TABLE_COLUMN_ID + " = " + keyId;
        //ret = db.update(MEASUREMENT_LIST_TABLE, contentValues, where, null);
        //} else {
        ret = (int)db.insert(MEASUREMENT_LIST_TABLE, null, contentValues);
        //}
        return ret;
    }

    public void saveMestDatas(List<MeasurementCountData> datas){
        int insertCount = 0;
        Log.d(TAG, "saveRecords");
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }
        mWritableDatabase.execSQL("delete from "+MEASUREMENT_LIST_TABLE);
        mWritableDatabase.beginTransaction();
        for (MeasurementCountData data : datas) {
            insertCount += insertRecord(mWritableDatabase, data);
        }
        mWritableDatabase.setTransactionSuccessful();
        mWritableDatabase.endTransaction();
        if(insertCount > 0)
            mContext.getContentResolver().notifyChange(MEASUREMENT_LIST_TABLE_URI, null);
    }

    public boolean saveMestData(MeasurementCountData data){
        boolean successfullySaved = false;
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }

        if (insertRecord(mWritableDatabase, data) > 0) {
            successfullySaved = true;
            mContext.getContentResolver().notifyChange(MEASUREMENT_LIST_TABLE_URI, null);
        }
        return successfullySaved;
    }

    public int loadMestDatasFromServer(String devId) {
        int ret = -1;
        int allCount = -1;

        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_ID, devId);

        /*At least 1 page.*/
        JSONObject result = loadContentPageFromServer(Constant.URL_FETCH_RECORD_VIEW, parameters);
        if(result != null){
            try {
                JSONObject resultJSONObject = result.getJSONObject("UserInfo").getJSONObject("ret");
                ret = resultJSONObject.getInt("status_code");
                if(ret != ErrorCodeMap.ERRNO_FETCH_RECORD_SUCCESSFULLY ) return -1;
            } catch (JSONException e) {
                e.printStackTrace();

            }
        }
        ret = parserReportMsgResult(result, true, devId);
        return ret;
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

    private int parserReportMsgResult(JSONObject data, boolean isRecord,String devId) {
        JSONObject userInfoJsonObject = null;
        int ret = -1;
        try {
            JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
            JSONObject jsondataObject = resultObject.getJSONObject("data");
            JSONArray jsondayArray = jsondataObject.getJSONArray("7dayvalue");
            JSONArray jsonweekArray = jsondataObject.getJSONArray("7weekvalue");
            JSONArray jsonmonthArray = jsondataObject.getJSONArray("7monthvalue");
            JSONArray jsonyearArray = jsondataObject.getJSONArray("3yearvalue");
            ret = jsondayArray.length()+jsonweekArray.length()+jsonmonthArray.length()+jsonyearArray.length();
            saveRecordsFromReportmsg(jsondayArray,jsonweekArray,jsonmonthArray,jsonyearArray,devId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void saveRecordsFromReportmsg(JSONArray jsondayArray,JSONArray jsonweekArray,JSONArray jsonmonthArray,JSONArray jsonyearArray,String devId) {
        List<MeasurementCountData> datas = new ArrayList<>();
        try {
            for (int i = 0; i < jsondayArray.length(); i++) {
                JSONObject measurementJsonObject =
                        jsondayArray.getJSONObject(i);
                MeasurementCountData data = new MeasurementCountData(measurementJsonObject,devId,TABLE_TYPE_DAY);
                datas.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < jsonweekArray.length(); i++) {
                JSONObject measurementJsonObject =
                        jsonweekArray.getJSONObject(i);
                MeasurementCountData data = new MeasurementCountData(measurementJsonObject,devId,TABLE_TYPE_WEEK);
                datas.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < jsonmonthArray.length(); i++) {
                JSONObject measurementJsonObject =
                        jsonmonthArray.getJSONObject(i);
                MeasurementCountData data = new MeasurementCountData(measurementJsonObject,devId,TABLE_TYPE_MONTH);
                datas.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            for (int i = 0; i < jsonyearArray.length(); i++) {
                JSONObject measurementJsonObject =
                        jsonyearArray.getJSONObject(i);
                MeasurementCountData data = new MeasurementCountData(measurementJsonObject,devId,TABLE_TYPE_YEAR);
                datas.add(data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDatabaseHelper.saveMestDatas(datas);
    }
}
