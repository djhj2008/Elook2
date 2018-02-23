package com.elook.client.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.elook.client.exception.ErrorCodeMap;
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
public class PushMessageService {
    private static final String TAG = "PushMessageService";
    public static final String PUSH_MESSAGE_TABLE ="pushmsg";
    public static final Uri PUSHMSG_TABLE_URI = Uri.parse(ELookDatabaseHelper.BASE_URI + PUSH_MESSAGE_TABLE);

    private static final String KEY_ID = "_id";
    private static final String PUSHMSG_TABLE_ID = "idPushmsg";
    private static final String PUSHMSG_STATE = "state";
    private static final String PUSHMSG_TYPE = "type";
    private static final String PUSHMSG_TITLE = "title";
    private static final String PUSHMSG_MSG = "msg";
    private static final String PUSHMSG_TIMESTAMP = "time";
    private static final String PUSHMSG_USER = "Push_user";

    public static final String PUSHMSG_ID = "pushid";
    private static final int THREAD_POOL_SIZE = 5;

    public static final String CREATE_PUSHMSG_TABLE_SQL = "CREATE TABLE "+ PUSH_MESSAGE_TABLE + " ("+
            PUSHMSG_TABLE_ID +" integer PRIMARY KEY NOT NULL, "+
            PUSHMSG_STATE + " integer not null, "+
            PUSHMSG_TYPE + " integer not null, "+
            PUSHMSG_TITLE + " text not null, " +
            PUSHMSG_MSG + " text, " +
            PUSHMSG_TIMESTAMP + " text not null, "+
            PUSHMSG_USER + " text REFERENCES "+UserInfoService.USERS_TABLE +"("+UserInfoService.TABLE_COLUMN_ID +") " +
            ");";


    ExecutorService mThreadPool;
    ELookDatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mReadableDatabase, mWritableDatabase;
    private Context mContext;


    public PushMessageService(Context c,ELookDatabaseHelper databaseHelper){
        mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mDatabaseHelper = databaseHelper;
        this.mContext = c;
    }

    private boolean isPushMessageExisted(int msgId){
        if(msgId < 0){
            Log.e(TAG, "Message Id is empty. Cannot query");
            return false;
        }
        boolean isExisted = false;
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        /*select * from MEASUREMENT_RECORDS_TABLE where RECORD_TIMESTAMP = recordate;  */
        final String SQL = "select distinct  * from "+ PUSH_MESSAGE_TABLE +
                " where "+ PUSHMSG_TABLE_ID +" = "+ msgId + ";";
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);
        if (cursor.getCount() > 0)  isExisted = true;
        cursor.close();
        return  isExisted;
    }

    private int insertPushMessage(SQLiteDatabase db, PushMessage message){
        if(isPushMessageExisted(message.getPushMsgId())) {
            return 0 ;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(PUSHMSG_TABLE_ID, message.getPushMsgId());
        contentValues.put(PUSHMSG_STATE,message.getState());
        contentValues.put(PUSHMSG_TYPE, message.getPushMsgType());
        contentValues.put(PUSHMSG_TITLE, message.getPushMsgTitle());
        if(message.getPushMsgBody() != null && !message.getPushMsgBody().isEmpty()){
            contentValues.put(PUSHMSG_MSG, message.getPushMsgBody());
        }
        contentValues.put(PUSHMSG_TIMESTAMP, message.getPushMsgTimestamp());
        contentValues.put(PUSHMSG_USER, message.getPushMsgUserId());
        int count = (int)db.insert(PUSH_MESSAGE_TABLE, null, contentValues);
        return count;
    };

    public boolean savePushMessage(PushMessage message) {
        if(isPushMessageExisted(message.getPushMsgId())) return true;
        boolean successfullySaved = false;
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }

        if (insertPushMessage(mWritableDatabase, message) > 0) {
            successfullySaved = true;
            mContext.getContentResolver().notifyChange(PUSHMSG_TABLE_URI, null);
        }
        return successfullySaved;
    }

//    private void setPushMessageReadState(int msgId){
//        final String SQL = "update "+ PUSH_MESSAGE_TABLE+ "  set "+ PUSHMSG_TYPE +" = 2"+ " where "+ PUSHMSG_TABLE_ID + " = " + msgId + ";";
//        if(mWritableDatabase == null){
//            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
//        }
//        mWritableDatabase.execSQL(SQL);
//        mContext.getContentResolver().notifyChange(PUSHMSG_TABLE_URI, null);
//    }

    public void savePushMessages(List<PushMessage> messages){
        int insertCount = 0;
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }
        mWritableDatabase.execSQL("delete from "+PUSH_MESSAGE_TABLE);
        mWritableDatabase.beginTransaction();
        for (PushMessage msg : messages) {
            insertCount += insertPushMessage(mWritableDatabase, msg);
        }
        mWritableDatabase.setTransactionSuccessful();
        mWritableDatabase.endTransaction();
        if(insertCount > 0)
            mContext.getContentResolver().notifyChange(PUSHMSG_TABLE_URI, null);
    }

    public void savePushMessageBody(int msgId, String msgBody, int state){
        if(isPushMessageExisted(msgId)){
            ContentValues contentValues = new ContentValues();
            contentValues.put(PUSHMSG_MSG, msgBody);
            contentValues.put(PUSHMSG_STATE,state);//doujun add
            String where = PUSHMSG_TABLE_ID + " = " + msgId;

            if(mWritableDatabase == null){
                mWritableDatabase = mDatabaseHelper.getWritableDatabase();
            }
            mWritableDatabase.update(PUSH_MESSAGE_TABLE, contentValues, where, null);
            mContext.getContentResolver().notifyChange(PUSHMSG_TABLE_URI, null);
        }
    }

    public PushMessage getMessage (int pushMessageId){
        int mPushMsgType = -1;
        int mPushMsgstate;
        String mPushMsgTitle = ""; //msg
        String mPushMsgBody = "";// body
        String mPushMsgTimestamp = "";
        PushMessage message = new PushMessage();
        message.setPushMsgUserId(mDatabaseHelper.getActiveUserInfo().getUserId());
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        Cursor cursor = mReadableDatabase.query(true, PUSH_MESSAGE_TABLE, null,
                PUSHMSG_TABLE_ID + "=?", new String[]{pushMessageId + ""}, null, null, null, null);
        Log.d(TAG, "cursor count = "+cursor.getCount());
        if(cursor.getCount() == 1){
            cursor.moveToFirst();
            mPushMsgType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(PUSHMSG_TYPE)));
            mPushMsgTitle = cursor.getString(cursor.getColumnIndex(PUSHMSG_TITLE));
            mPushMsgBody = cursor.getString(cursor.getColumnIndex(PUSHMSG_MSG));
            mPushMsgstate = cursor.getInt(cursor.getColumnIndex(PUSHMSG_STATE));
            message.setState(mPushMsgstate);
            message.setPushMsgId(pushMessageId);
            message.setPushMsgType(mPushMsgType);
            message.setPushMsgTitle(mPushMsgTitle);
            if(mPushMsgBody != null ){
                Log.d(TAG, "msgBody = "+mPushMsgBody);
                message.setPushMsgBody(mPushMsgBody);
            }
            mPushMsgTimestamp = cursor.getString(cursor.getColumnIndex(PUSHMSG_TIMESTAMP));
            message.setPushMsgTimestamp(mPushMsgTimestamp);
            //setPushMessageReadState(pushMessageId);
        }
        cursor.close();
        return message;
    }

    public List<PushMessage> getMessages(int uid){
        int msgId = -1, msgType = -1,msgState = -1;
        String msgTitle = "", msgBody = "", msgTimestamp = "";

        List<PushMessage> messages = new ArrayList<>();
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        String SQL = "select distinct * from "+PUSH_MESSAGE_TABLE+" where " + PUSHMSG_USER +" = "+uid+";";
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                msgId = Integer.parseInt(cursor.getString(cursor.getColumnIndex(PUSHMSG_TABLE_ID)));
                msgState = Integer.parseInt(cursor.getString(cursor.getColumnIndex(PUSHMSG_STATE)));
                msgType = Integer.parseInt(cursor.getString(cursor.getColumnIndex(PUSHMSG_TYPE)));
                msgTimestamp = cursor.getString(cursor.getColumnIndex(PUSHMSG_TIMESTAMP));
                msgTitle = cursor.getString(cursor.getColumnIndex(PUSHMSG_TITLE));
                msgBody = cursor.getString(cursor.getColumnIndex(PUSHMSG_MSG));
                PushMessage msg = new PushMessage(msgId,msgState, msgType, msgTitle,msgBody, msgTimestamp, uid);
                if(msgBody != null && !msgBody.isEmpty())msg.setPushMsgBody(msgBody);
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

    private int getAllPageNumbers(JSONObject resultJsonObject, boolean isRecord) {
        int allPageNumbers = -1;
        JSONObject userInfoJsonObject = null;
        String ret = "";
        try {
            userInfoJsonObject = resultJsonObject.getJSONObject("UserInfo");
            ret = userInfoJsonObject.getString("ret");
            if ((isRecord && !ret.trim().equals(Constant.ErrorCode.CHECK_DEVICE_SUCCESSFULLY_MESSAGE)) ||
                    (!isRecord && !ret.trim().equals(Constant.ErrorCode.CHECK_DEVID_SUCCESSFULLY_MESSAGE))) {
                Log.e(TAG, "Cannot get correct data");
                return -1;
            }
            allPageNumbers = Integer.parseInt(userInfoJsonObject.getString("pagenum"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return allPageNumbers;
    }

    private int parserMessageContentPageResult(JSONObject data,int uid) {
        int ret = -1;
        try {
            JSONObject resultJSONObject = data.getJSONObject("UserInfo").getJSONObject("ret");
            ret = resultJSONObject.getInt("status_code");
            if ( ret != ErrorCodeMap.ERRNO_FETCH_PUSHMSG_SUCCESSFULLY) {
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
        List<PushMessage> messages = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                JSONObject measurementJsonObject =
                        infooneJsonObject.getJSONObject(i);
                PushMessage msg = new PushMessage(measurementJsonObject, uid);
                messages.add(msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mDatabaseHelper.savePushMessages(messages);
    }

    public int loadPushmsgsFromServer(int uid){
        int ret = -1;

        Map<String, String> parameters = new Hashtable<String, String>();
        parameters.put("pushuserid", uid+"");

        JSONObject result = loadContentPageFromServer(Constant.URL_FETCH_PUSHMSG, parameters);
        ret = parserMessageContentPageResult(result,uid);

        return ret;
    }

    public int fetchSiglePushMessage(int pushid) throws RemoteException {
        int fetchSingleMessageRet = -1;
        int ret = 0;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(PUSHMSG_ID, pushid + "");
        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_FETCH_SIGLE_MSG, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            JSONObject resultJSONObject = data.getJSONObject("UserInfo").getJSONObject("ret");
            ret = resultJSONObject.getInt("status_code");
            if ( ret != ErrorCodeMap.ERRNO_FETCH_PUSHMSGID_SUCCESSFULLY) {
                Log.e(TAG, "Cannot get correct data");
                return -1;
            }
            JSONObject info = resultJSONObject.getJSONObject("info");
            String msg = info.getString("pushmsg_msg");
            String tmp = info.getString("pushmsg_state");
            int state=0;
            if(tmp!=null){
                state = Integer.parseInt(tmp);
            }
            Log.d(TAG, "msg = " + msg);
            mDatabaseHelper.savePushMessageBody(pushid, msg,state);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fetchSingleMessageRet;
    }
}
