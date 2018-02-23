package com.elook.client.service;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.elook.client.user.AdvertInfo;
import com.elook.client.user.MeasurementCountData;
import com.elook.client.user.ProblemMsg;
import com.elook.client.user.PushMessage;
import com.elook.client.user.AccessPointInfo;
import com.elook.client.user.MeasurementInfo;
import com.elook.client.user.MeasurementRecord;
import com.elook.client.user.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by guoguo on 2016/3/6.
 */
/*
* Connected Access Point table, include password;
* Configured Device
* */
public class ELookDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "ELookDatabaseHelper";
    public static final String ELOOK_DATABASE = "settings";
    public static final String BASE_URI = "content://com.elook.client/"+ELOOK_DATABASE+"/";
    private static final int DB_VERSION = 1;

    private static ELookDatabaseHelper sELookDatabaseHelper;
    private UserInfoService mUserTable;
    private PushMessageService mPushMessageTable;
    private MeasurementRecordService mMeasurementRecordTable;
    private MeasurementInfoService mMeasurementInfoTable;
    private MeasurementListService mMeasurementListTable;
    private AccessPointTable mAccessPiontTable;
    private ProblemMsgService mProblemMsgTable;

    private Context mContext;
    SQLiteDatabase mDatabse;
    private ELookDatabaseHelper(Context context){
        super(context, ELOOK_DATABASE, null, DB_VERSION);
        this.mContext = context;
        initTables();
    }

    private ELookDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
    }

    private ELookDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
        this.mContext = context;
    }

    public static ELookDatabaseHelper newInstance(Context context){
        if(sELookDatabaseHelper == null){
            sELookDatabaseHelper = new ELookDatabaseHelper(context);
        }

        return sELookDatabaseHelper;
    }

    @Override
    public void onOpen(SQLiteDatabase db) {

    }

    private  void initTables(){
        mUserTable = new UserInfoService(mContext, this);
        mPushMessageTable = new PushMessageService(mContext, this);
        mMeasurementRecordTable = new MeasurementRecordService(mContext, this);
        mMeasurementInfoTable = new MeasurementInfoService(mContext, this);
        mMeasurementListTable = new MeasurementListService(mContext, this);
        mAccessPiontTable = new AccessPointTable(mContext, this);
        mProblemMsgTable =  new ProblemMsgService(mContext,this);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(AccessPointTable.CREATE_AP_TABLE_SQL);
            db.execSQL(UserInfoService.CREATE_USERS_TABLE_SQL);
            db.execSQL(UserInfoService.CREATE_ADVERT_TABLE_SQL);
            db.execSQL(MeasurementInfoService.CREATE_MEASUREMENTS_TABLE_SQL);
            db.execSQL(MeasurementListService.CREATE_MEASUREMENT_LIST_SQL);
            db.execSQL(MeasurementRecordService.CREATE_MEASUREMENT_RECORDS_SQL);
            db.execSQL(PushMessageService.CREATE_PUSHMSG_TABLE_SQL);
            db.execSQL(ProblemMsgService.CREATE_PROBLEMMSG_TABLE_SQL);
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public UserInfo getActiveUserInfo(){
        UserInfo info = null;
        if(mUserTable != null){
            info = mUserTable.getActiveUserInfo();
        }
        return  info;
    }

    public String getUserPassword(UserInfo userInfo){
        String passwd = null;
        if(mUserTable != null){
            passwd = mUserTable.getUserPassword(userInfo);
        }
        return  passwd;
    }

    public void updateActiveUser(UserInfo activeUserInfo){
        if(mUserTable != null){
            mUserTable.updateActiveUser(activeUserInfo);
        }
    }

    public void disableAllUserActived(){
        if(mUserTable != null){
            mUserTable.disableAllUserActived();
        }
    }

    public boolean saveAdvertInfo(AdvertInfo advertInfo){
        boolean isSuccessful = false;
        if(mUserTable != null){
            isSuccessful = mUserTable.saveAdvertInfo(advertInfo);
        }
        return isSuccessful;
    }

    public List<AdvertInfo> getAdvertInfo(){
        List<AdvertInfo> infos = new ArrayList<>();
        if(mUserTable != null){
            infos = mUserTable.getAdvertInfo();
        }
        return infos;
    }

    public boolean saveUserInfo(UserInfo userInfo){
        boolean isSuccessful = false;
        if(mUserTable != null){
            isSuccessful = mUserTable.saveUserInfo(userInfo);
        }
        return isSuccessful;
    }

    public boolean saveMeasurementInfo(MeasurementInfo info){
        boolean isSuccessful = false;
        if(mMeasurementInfoTable != null){
            isSuccessful = mMeasurementInfoTable.saveMeasurementInfo(info);
        }
        return isSuccessful;
    }

    public boolean saveMeasurementConfig(MeasurementInfo info){
        boolean isSuccessful = false;
        if(mMeasurementInfoTable != null){
            isSuccessful = mMeasurementInfoTable.saveMeasurementConfig(info);
        }
        return isSuccessful;
    }

    public boolean saveMeasurementInfoOther(MeasurementInfo info){
        boolean isSuccessful = false;
        if(mMeasurementInfoTable != null){
            isSuccessful = mMeasurementInfoTable.saveMeasurementInfoOther(info);
        }
        return isSuccessful;
    }

    public MeasurementInfo getMeasurementInfo(int devId){
        MeasurementInfo info = null;
        if(mMeasurementInfoTable != null){
            info = mMeasurementInfoTable.getMeasurementInfo(devId);
        }
        return info;
    }

    public void cleanMeasurementInfos(){
        Log.d(TAG, "cleanMeasurementInfos");
        if(mMeasurementInfoTable != null){
            mMeasurementInfoTable.cleanMeasurementInfos();
        }
    }

    public void saveMeasurementInfos(List<MeasurementInfo> infos){
        Log.d(TAG, "saveMeasurementInfos");
        if(mMeasurementInfoTable != null){
            mMeasurementInfoTable.saveMeasurementInfos(infos);
        }
    }

    public List<MeasurementInfo> getMeasurementsOfUser(String userphonename){
        Log.d(TAG, "getMeasurementsOfUser, userphonename = "+userphonename);
        List<MeasurementInfo> measurements = new ArrayList<>();
        if(mMeasurementInfoTable != null){
            measurements = mMeasurementInfoTable.getMeasurementsOfUser(userphonename);
        }
        return measurements;
    }

    public void saveProblemMsgs(List<ProblemMsg> msg){
        if(mProblemMsgTable != null){
            mProblemMsgTable.saveProblemMessages(msg);
        }
    }
    public List<ProblemMsg> getProblemMessages(){
        List<ProblemMsg> messages = new ArrayList<>();
        if(mProblemMsgTable != null){
            messages = mProblemMsgTable.getMessages();
        }
        return messages;
    }

    public void savePushMessageBody(int msgId, String msgBody,int state){
        if(mPushMessageTable != null){
            mPushMessageTable.savePushMessageBody(msgId, msgBody,state);
        }
    }

    public void savePushMessages(List<PushMessage> messages){
        if(mPushMessageTable != null){
            mPushMessageTable.savePushMessages(messages);
        }
    }

    public PushMessage getMessage (int pushMessageId){
        PushMessage message = null;
        if(mPushMessageTable != null){
            message = mPushMessageTable.getMessage(pushMessageId);
        }
        return message;
    }

    public List<PushMessage> getMessages(int uid){
        List<PushMessage> messages = new ArrayList<>();
        if(mPushMessageTable != null){
            messages = mPushMessageTable.getMessages(uid);
        }
        return messages;
    }

    public boolean saveApPasswd(AccessPointInfo apInfo, String password){
        boolean isSuccessfull = false;
        if(mAccessPiontTable != null){
            isSuccessfull = mAccessPiontTable.saveApPasswd(apInfo, password);
        }
        return isSuccessfull;
    }

    public AccessPointInfo getActivedApInfo(){
        AccessPointInfo activedApInfo =  null;
        if(mAccessPiontTable != null){
            activedApInfo = mAccessPiontTable.getActivedApInfo();
        }
        return activedApInfo;
    }

    public String getPasswordOfAp(AccessPointInfo apInfo){
        String passwd = "";
        if(mAccessPiontTable != null){
            passwd = mAccessPiontTable.getPasswordOfAp(apInfo);
        }
        return passwd;
    }

    public List<AccessPointInfo> getConnectedAps(){
        List<AccessPointInfo> hasConnectedApInfos = new ArrayList<>();
        if(mAccessPiontTable != null){
            hasConnectedApInfos = mAccessPiontTable.getConnectedAps();
        }
        return hasConnectedApInfos;
    }

    public void saveMestDatas(List<MeasurementCountData> datas){
        if(mMeasurementListTable != null){
            mMeasurementListTable.saveMestDatas(datas);
        }
    }

    public List<MeasurementCountData> getMestDatas(int devId,int type) {
        List<MeasurementCountData> datas = new ArrayList<>();
        if(mMeasurementRecordTable != null){
            datas = mMeasurementListTable.getMeasurementDayDatas(devId, type);
        }
        return datas;
    }

    public void saveRecords(List<MeasurementRecord> records){
        if(mMeasurementRecordTable != null){
            mMeasurementRecordTable.saveRecords(records);
        }
    }


    public long getRecordsCount(String devId) {
        long count = 0;
        if(mMeasurementRecordTable != null){
            count = mMeasurementRecordTable.getRecordsCount(devId);
        }
        return count;
    }

    public int getPagesOfRecords(String devId){
        int count = 0;
        if(mMeasurementRecordTable != null){
            count = mMeasurementRecordTable.getPagesOfRecords(devId);
        }
        return count;
    }


    public List<MeasurementRecord> getRecords(int devId) {
        List<MeasurementRecord> records = new ArrayList<>();
        if(mMeasurementRecordTable != null){
            records = mMeasurementRecordTable.getRecords(devId);
        }
        return records;
    }
}
