package com.elook.client.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.elook.client.exception.ErrorCodeMap;
import com.elook.client.user.ProblemMsg;
import com.elook.client.user.PushMessage;
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
public class ProblemMsgService {
    private static final String TAG = "ProblemMsgService";
    public static final String PROBLEM_MESSAGE_TABLE ="problemmsg";
    public static final Uri PROBLEMMSG_TABLE_URI = Uri.parse(ELookDatabaseHelper.BASE_URI + PROBLEM_MESSAGE_TABLE);

    private static final String KEY_ID = "_id";
    private static final String PROBLEMMSG_TABLE_ID = "idPushmsg";
    private static final String PROBLEMMSG_TITLE = "title";
    private static final String PROBLEMMSG_MSG = "msg";


    private static final int THREAD_POOL_SIZE = 5;

    public static final String CREATE_PROBLEMMSG_TABLE_SQL = "CREATE TABLE "+ PROBLEM_MESSAGE_TABLE + " ("+
            PROBLEMMSG_TABLE_ID +" integer PRIMARY KEY NOT NULL, "+
            PROBLEMMSG_TITLE + " text not null, " +
            PROBLEMMSG_MSG + " text " +
            ");";


    ExecutorService mThreadPool;
    ELookDatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mReadableDatabase, mWritableDatabase;
    private Context mContext;


    public ProblemMsgService(Context c, ELookDatabaseHelper databaseHelper){
        mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mDatabaseHelper = databaseHelper;
        this.mContext = c;
    }

    private boolean isProblemMsgExisted(int msgId){
        if(msgId < 0){
            Log.e(TAG, "Message Id is empty. Cannot query");
            return false;
        }
        boolean isExisted = false;
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        /*select * from MEASUREMENT_RECORDS_TABLE where RECORD_TIMESTAMP = recordate;  */
        final String SQL = "select distinct  * from "+ PROBLEM_MESSAGE_TABLE +
                " where "+ PROBLEMMSG_TABLE_ID +" = "+ msgId + ";";
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);
        if (cursor.getCount() > 0)  isExisted = true;
        cursor.close();
        return  isExisted;
    }

    private int insertProblemMessage(SQLiteDatabase db, ProblemMsg message){
        if(isProblemMsgExisted(message.getProblemMsgId())) {
            return 0 ;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(PROBLEMMSG_TABLE_ID, message.getProblemMsgId());
        contentValues.put(PROBLEMMSG_TITLE, message.getProblemMsgTitle());
        contentValues.put(PROBLEMMSG_MSG, message.getProblemMsgBody());
        int count = (int)db.insert(PROBLEM_MESSAGE_TABLE, null, contentValues);
        return count;
    };

//    public boolean saveProblemMessage(ProblemMsg message) {
//        if(isProblemMsgExisted(message.getProblemMsgId())) return true;
//        boolean successfullySaved = false;
//        if(mWritableDatabase == null){
//            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
//        }
//
//        if (insertProblemMessage(mWritableDatabase, message) > 0) {
//            successfullySaved = true;
//            mContext.getContentResolver().notifyChange(PROBLEMMSG_TABLE_URI, null);
//        }
//        return successfullySaved;
//    }

    public void saveProblemMessages(List<ProblemMsg> messages){
        int insertCount = 0;
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }
        mWritableDatabase.execSQL("delete from "+PROBLEM_MESSAGE_TABLE);
        mWritableDatabase.beginTransaction();
        for (ProblemMsg msg : messages) {
            insertCount += insertProblemMessage(mWritableDatabase, msg);
        }
        mWritableDatabase.setTransactionSuccessful();
        mWritableDatabase.endTransaction();
        if(insertCount > 0)
            mContext.getContentResolver().notifyChange(PROBLEMMSG_TABLE_URI, null);
    }

    public List<ProblemMsg> getMessages(){
        int msgId = -1, msgType = -1,msgState = -1;
        String msgTitle = "", msgBody = "", msgTimestamp = "";

        List<ProblemMsg> messages = new ArrayList<>();
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        String SQL = "select distinct * from "+PROBLEM_MESSAGE_TABLE+";";
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                msgId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(PROBLEMMSG_TABLE_ID)));
                msgTitle = cursor.getString(cursor.getColumnIndex(PROBLEMMSG_TITLE));
                msgBody = cursor.getString(cursor.getColumnIndex(PROBLEMMSG_MSG));
                ProblemMsg msg = new ProblemMsg(msgId, msgTitle,msgBody);
                Log.d(TAG,"getMessages:msgId:"+msgId+" msgState:"+msgState+" msgType:"+msgType);
                messages.add(msg);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return messages;
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

    private int parserMessageContentPageResult(JSONObject data,int uid) {
        int ret = -1;
        try {
            JSONObject resultJSONObject = data.getJSONObject("UserInfo").getJSONObject("ret");
            ret = resultJSONObject.getInt("status_code");
            if ( ret != ErrorCodeMap.ERRNO_FETCH_PROBLEMMSG_SUCCESSFULLY) {
                Log.e(TAG, "Cannot get correct data");
                return -1;
            }
            JSONArray jsonArray = resultJSONObject.getJSONArray("data");
            saveMessageFromPage(jsonArray, jsonArray.length(),uid);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 1;
    }

    private void saveMessageFromPage(JSONArray infooneJsonObject, int count , int uid) {
        List<ProblemMsg> messages = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                JSONObject measurementJsonObject =
                        infooneJsonObject.getJSONObject(i);
                ProblemMsg msg = new ProblemMsg(measurementJsonObject, uid);
                messages.add(msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDatabaseHelper.saveProblemMsgs(messages);
    }

    public int loadProblemMsgServer(int uid){
        int ret = -1;

        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put("enduserid", uid+"");

        JSONObject result = loadContentPageFromServer(Constant.URL_FETCH_PROBLEMMSG, parameters);
        ret = parserMessageContentPageResult(result,uid);

        return ret;
    }

}
