package com.elook.client.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.elook.client.exception.ErrorCodeMap;
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
public class MeasurementRecordService {
    private static final String TAG = "MeasurementRecord";
    public static final String MEASUREMENT_RECORDS_TABLE = "measurement_records";
    public static final Uri MEASUREMENT_RECORDS_TABLE_URI = Uri.parse(ELookDatabaseHelper.BASE_URI+MEASUREMENT_RECORDS_TABLE);

    /*
{   "access_autoid":"6",
    "access_device_id":"123456789",
    "access_value":"321",
    "access_time":"1464332775",
    "access_new_url":null
}
*/
    public static final String TABLE_COLUMN_ID = "access_autoid";
    public static final String TABLE_COLUMN_DEV_ID = "access_device_id";
    public static final String TABLE_COLUMN_DEV_VALUE = "access_value";
    public static final String TABLE_COLUMN_TIMESTAMP = "access_time";
    public static final String TABLE_COLUMN_PIC_URL = "access_new_url";


    public static final String CREATE_MEASUREMENT_RECORDS_SQL = "CREATE TABLE "+ MEASUREMENT_RECORDS_TABLE + " ("+
            TABLE_COLUMN_ID +" integer not null , "+
            TABLE_COLUMN_DEV_ID + " text, "+
            TABLE_COLUMN_DEV_VALUE + " text not null, " +
            TABLE_COLUMN_TIMESTAMP + " integer not null, "+
            TABLE_COLUMN_PIC_URL + " text" +
            ");";

    private static final int THREAD_POOL_SIZE = 5;

    ExecutorService mThreadPool;
    ELookDatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mReadableDatabase, mWritableDatabase;
    private Context mContext;

    public MeasurementRecordService(Context c, ELookDatabaseHelper databaseHelper){
        mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mDatabaseHelper = databaseHelper;
        this.mContext = c;
    }

    final int PAGE_RECORD_COUNT = 20;
    public int getPagesOfRecords(String devId){
        long count = getRecordsCount(devId);
        return (int)Math.ceil(((float)count) / PAGE_RECORD_COUNT);
    }

    public long getRecordsCount(String devId) {
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        String sql = "SELECT COUNT(*) FROM " + MEASUREMENT_RECORDS_TABLE + " where " +
                TABLE_COLUMN_DEV_ID + " = "+devId + " ;";
        Cursor cursor = mReadableDatabase.rawQuery(sql, null);
        cursor.moveToFirst();
        long count = cursor.getLong(0);
        return count;
    }

    public MeasurementRecord getLatestRecrd(int devId){
        int value = 0;
        int datetime;
        int devid;
        String picUrl = "";
        MeasurementRecord record = new MeasurementRecord();
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        String SQL = "select distinct * from "+MEASUREMENT_RECORDS_TABLE+" where " + TABLE_COLUMN_DEV_ID+" = "+devId +
                " and max("+TABLE_COLUMN_TIMESTAMP+")"+";";

        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                value = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_VALUE));
                datetime = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_TIMESTAMP));
                int recordId = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_ID));
                record.setRecordId(recordId);
                record.setDevId(devId);
                record.setDateTime(datetime);
                record.setValues(value);
                picUrl = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_PIC_URL));
                if(picUrl != null && !picUrl.isEmpty())
                    record.setNewPicUrl(picUrl);
            }while (cursor.moveToNext());
        } else {

        }
        cursor.close();
        return record;
    }

    public List<MeasurementRecord> getRecords(int devId) {
        int value = 0;
        int datetime;
        int devid;
        String picUrl = "";
        List<MeasurementRecord> records = new ArrayList<>();
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        String SQL = "select distinct * from "+MEASUREMENT_RECORDS_TABLE+" where " + TABLE_COLUMN_DEV_ID+" = "+devId+";";
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                devid = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_ID));
                value = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_DEV_VALUE));
                datetime = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_TIMESTAMP));
                int recordId = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_ID));
                MeasurementRecord record = new MeasurementRecord(recordId,devid, datetime, value);
                picUrl = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_PIC_URL));
                if(picUrl != null && !picUrl.isEmpty())
                    record.setNewPicUrl(picUrl);
                records.add(record);
            }while (cursor.moveToNext());
        } else {

        }
        cursor.close();
        return records;
    }

    private int isRecordExisted(MeasurementRecord record){
        if(record == null){
            Log.e(TAG, "Record is empty. Cannot query");
            return -1;
        }
        boolean isExisted = false;
        int recordDate = record.getDateTime();

        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        /*select * from MEASUREMENT_RECORDS_TABLE where RECORD_TIMESTAMP = recordate;  */
        final String SQL = "select distinct  * from "+ MEASUREMENT_RECORDS_TABLE +
                " where "+ TABLE_COLUMN_ID +" = "+ record.getRecordId()+ ";";
        int recordId = -1;
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);
        if (cursor.getCount() > 0)  {
            recordId = record.getRecordId();
        }else {
            recordId = -1;
        }
        cursor.close();
        return  recordId;
    }
    private int insertRecord(SQLiteDatabase db, MeasurementRecord record){
        int keyId = isRecordExisted(record);
        boolean isExisted = keyId > 0 ? true: false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMN_ID, record.getRecordId());
        contentValues.put(TABLE_COLUMN_DEV_ID, record.getDevId());
        contentValues.put(TABLE_COLUMN_DEV_VALUE, record.getRecordValue());
        contentValues.put(TABLE_COLUMN_TIMESTAMP, record.getDateTime());
        contentValues.put(TABLE_COLUMN_PIC_URL, record.getNewPicUrl());

        int ret = 0;
        if(isExisted){
            String where = TABLE_COLUMN_ID + " = " + keyId;
            ret = db.update(MEASUREMENT_RECORDS_TABLE, contentValues, where, null);
        } else {
            ret = (int)db.insert(MEASUREMENT_RECORDS_TABLE, null, contentValues);
        }
        return ret;
    }

    public void saveRecords(List<MeasurementRecord> records){
        int insertCount = 0;
        Log.d(TAG, "saveRecords");
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }

        mWritableDatabase.beginTransaction();
        for (MeasurementRecord record : records) {
            insertCount += insertRecord(mWritableDatabase, record);
        }
        mWritableDatabase.setTransactionSuccessful();
        mWritableDatabase.endTransaction();
        if(insertCount > 0)
            mContext.getContentResolver().notifyChange(MEASUREMENT_RECORDS_TABLE_URI, null);
    }

    public boolean saveRecord(MeasurementRecord record){
        boolean successfullySaved = false;
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }

        if (insertRecord(mWritableDatabase, record) > 0) {
            successfullySaved = true;
            mContext.getContentResolver().notifyChange(MEASUREMENT_RECORDS_TABLE_URI, null);
        }
        return successfullySaved;
    }

    public int loadRecordsFromServer(String devId, String startTime, String endTime) {
        int curPageNum = -1;
        int allPageNumbers = -1;
        int ret = -1;
        int allCount = -1;

        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_ID, devId);
        parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_PAGE, 1 + "");
        parameters.put(ELookServiceImpl.HTTP_PARAMS_TIME_START, startTime);
        parameters.put(ELookServiceImpl.HTTP_PARAMS_TIME_END, endTime);

        /*At least 1 page.*/
        JSONObject result = loadContentPageFromServer(Constant.URL_FETCH_RECORD, parameters);
        if(result != null){
            try {
                JSONObject resultJSONObject = result.getJSONObject("UserInfo").getJSONObject("ret");
                ret = resultJSONObject.getInt("status_code");
                if(ret != ErrorCodeMap.ERRNO_SELECT_DEVICE_SUCCESSFULLY ) return -1;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //ret = parserContentPageResult(result, true);

        allPageNumbers = getAllPageNumbers(result);
        Log.d(TAG, "allPageNumbers = "+allPageNumbers);
        for (int i = 1; i <= allPageNumbers; i++) {
            parameters = new Hashtable<String, String>();
            parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_ID, devId);
            parameters.put(ELookServiceImpl.HTTP_PARAMS_DEV_PAGE, i + "");
            parameters.put(ELookServiceImpl.HTTP_PARAMS_TIME_START, startTime);
            parameters.put(ELookServiceImpl.HTTP_PARAMS_TIME_END, endTime);
            result = loadContentPageFromServer(Constant.URL_FETCH_RECORD, parameters);
            ret = parserContentPageResult(result, true);
            parameters = null;
        }
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

    private int getAllPageNumbers(JSONObject data) {
        int allPageNumbers = -1;
        String ret = "";
        try {
            JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
            allPageNumbers = Integer.parseInt(resultObject.getString("pagenum"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allPageNumbers;
    }

    private int parserContentPageResult(JSONObject data, boolean isRecord) {
        int curPageNum = -1;
        JSONObject userInfoJsonObject = null;
        String ret = "";
        try {
            JSONObject resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
            curPageNum = Integer.parseInt(resultObject.getString("page"));
            JSONArray jsonArray = resultObject.getJSONArray("info");
            saveRecordsFromPage(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return curPageNum;
    }

    private void saveRecordsFromPage(JSONArray infooneJsonObject) {
        List<MeasurementRecord> records = new ArrayList<>();
        try {
            for (int i = 0; i < infooneJsonObject.length(); i++) {
                JSONObject measurementJsonObject =
                        infooneJsonObject.getJSONObject(i);
                MeasurementRecord record = new MeasurementRecord(measurementJsonObject);
                records.add(record);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDatabaseHelper.saveRecords(records);
    }
}
