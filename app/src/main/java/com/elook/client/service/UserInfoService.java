package com.elook.client.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.elook.client.exception.ErrorCode;
import com.elook.client.exception.ErrorCodeMap;
import com.elook.client.exception.ExceptionCenter;
import com.elook.client.user.AdvertInfo;
import com.elook.client.user.MeasurementCountData;
import com.elook.client.user.UserInfo;
import com.elook.client.utils.AESCrypt;
import com.elook.client.utils.Constant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by haiming on 5/20/16.
 */
public class UserInfoService {
    private static final String TAG = "UserInfoService";
    public static final String USERS_TABLE = "users";
    public static final String ADVERT_TABLE = "adverts";

    public static final Uri USERS_TABLE_URI = Uri.parse(ELookDatabaseHelper.BASE_URI+USERS_TABLE);
    public static final Uri ADVERT_TABLE_URI = Uri.parse(ELookDatabaseHelper.BASE_URI+USERS_TABLE);

    public static final String  TABLE_COLUMN_ID = "enduser_enduser_id";
    public static final String  TABLE_COLUMN_PHONE_NAME = "enduser_phone_name";
    public static final String  TABLE_COLUMN_ALIAS_NAME = "enduser_alias_name";
    public static final String  TABLE_COLUMN_MAIL = "enduser_mail";
    public static final String  TABLE_COLUMN_PWD = "enduser_manager_pwd";
    public static final String  TABLE_COLUMN_REGISTE_TIME = "enduser_register_time";
    public static final String  TABLE_COLUMN_LOGIN_STATUS = "enduser_login_status";
    public static final String  TABLE_COLUMN_COMMUNITY_ID = "enduser_community_id";
    public static final String  TABLE_COLUMN_REGION = "enduser_region";
    public static final String  TABLE_COLUMN_PROVINCE = "enduser_province";
    public static final String  TABLE_COLUMN_CITY = "enduser_city";
    public static final String  TABLE_COLUMN_AREA = "enduser_area";
    public static final String  TABLE_COLUMN_ADDR = "enduser_addr";
    public static final String  TABLE_COLUMN_LASTLOGIN = "enduser_is_last_login";

    //ADVERT
    public static final String  ADVERT_TABLE_COLUMN_ID = "easy_adv_autoid";
    public static final String  ADVERT_TABLE_COLUMN_PICURL = "easy_adv_picurl";
    public static final String  ADVERT_TABLE_COLUMN_ADVURL = "easy_adv_advurl";
    public static final String  ADVERT_TABLE_COLUMN_OPEN = "easy_adv_open";

    public static final String USERNAME_FIELD = "username";
    public static final String PASSWD_FIELD = "userpwd";
    public static final String USERID_FIELD = "id";
    public static final String KEY_CODE_FIELD = "code";
    public static final String NEWPASSWD_FIELD = "userpwdnew";


    private static final int THREAD_POOL_SIZE = 5;

    public static final String CREATE_USERS_TABLE_SQL = "CREATE TABLE "+ USERS_TABLE + " ("+
            TABLE_COLUMN_ID+ " integer not null UNIQUE, " +
            TABLE_COLUMN_PHONE_NAME + " text not null UNIQUE, " +
            TABLE_COLUMN_ALIAS_NAME + " text, " +
            TABLE_COLUMN_MAIL + " text,"+
            TABLE_COLUMN_PWD + " text, " +
            TABLE_COLUMN_REGISTE_TIME + " integer, "+
            TABLE_COLUMN_LOGIN_STATUS + " tinyint not null default 0, "+
            TABLE_COLUMN_COMMUNITY_ID +" integer, "+
            TABLE_COLUMN_REGION + " text,"+
            TABLE_COLUMN_PROVINCE + " text,"+
            TABLE_COLUMN_CITY + " text,"+
            TABLE_COLUMN_AREA + " text,"+
            TABLE_COLUMN_ADDR + " text, "+
            TABLE_COLUMN_LASTLOGIN + " tinyint default 0"+
            ");";

    public static final String CREATE_ADVERT_TABLE_SQL = "CREATE TABLE "+ ADVERT_TABLE + " ("+
            ADVERT_TABLE_COLUMN_ID+ " integer not null UNIQUE, " +
            ADVERT_TABLE_COLUMN_PICURL + " text not null, " +
            ADVERT_TABLE_COLUMN_ADVURL + " text, " +
            ADVERT_TABLE_COLUMN_OPEN + " integer"+
            ");";

    ExecutorService mThreadPool;
    ELookDatabaseHelper mDatabaseHelper;
    UserInfo mCurrentUser;
    private SQLiteDatabase mReadableDatabase, mWritableDatabase;
    private Context mContext;

    public UserInfoService(Context c,ELookDatabaseHelper databaseHelper){
        mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        mDatabaseHelper = databaseHelper;
        mCurrentUser = databaseHelper.getActiveUserInfo();
        this.mContext = c;
    }

    private int isUserExisted(String userName){
        int keyId = -1;
        if(userName.isEmpty()){
            Log.e(TAG, "username is empty. Cannot query");
            return keyId;
        }
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        Cursor cursor = mReadableDatabase.query(true, USERS_TABLE, new String[]{TABLE_COLUMN_ID, TABLE_COLUMN_PHONE_NAME},
                null, null, null, null, null, null);

        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                if (userName.equals(cursor.getString(1))){
                    keyId = cursor.getInt(0);
                }
            }while (cursor.moveToNext());
        }
        cursor.close();
        return keyId;
    }

    private int isUserExisted(UserInfo userInfo){
        return isUserExisted(userInfo.getUserPhoneName());
    }

    public void cleanAdverInfos(){
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }
        mWritableDatabase.execSQL("delete from "+ADVERT_TABLE);
    }


    public List<AdvertInfo> getAdvertInfo(){
        List<AdvertInfo> adverInfos = new ArrayList<>();
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        String SQL = "select distinct * from "+ADVERT_TABLE+";";
        Cursor cursor = mReadableDatabase.rawQuery(SQL, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                AdvertInfo info = new AdvertInfo();
                info.setAdvertId(cursor.getInt(cursor.getColumnIndex(ADVERT_TABLE_COLUMN_ID)));
                info.setAdvertPicUrl(cursor.getString(cursor.getColumnIndex(ADVERT_TABLE_COLUMN_PICURL)));
                info.setAdvertUrl(cursor.getString(cursor.getColumnIndex(ADVERT_TABLE_COLUMN_ADVURL)));
                info.setAdvertOpen(cursor.getInt(cursor.getColumnIndex(ADVERT_TABLE_COLUMN_OPEN)));
                adverInfos.add(info);
            }while (cursor.moveToNext());
        } else {

        }
        cursor.close();
        return adverInfos;
    }

    public boolean saveAdvertInfo(AdvertInfo advertInfo){
        boolean successfullySaved = false;

        ContentValues contentValues = new ContentValues();
        contentValues.put(ADVERT_TABLE_COLUMN_ID, advertInfo.getAdvertId());
        contentValues.put(ADVERT_TABLE_COLUMN_PICURL, advertInfo.getAdvertPicUrl());
        contentValues.put(ADVERT_TABLE_COLUMN_ADVURL,advertInfo.getAdvertUrl());
        contentValues.put(ADVERT_TABLE_COLUMN_OPEN, advertInfo.getAdvertOpen());

        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }

        if (mWritableDatabase.insert(ADVERT_TABLE, null, contentValues) > 0) {
            successfullySaved = true;
        }

        mContext.getContentResolver().notifyChange(ADVERT_TABLE_URI, null);
        return successfullySaved;
    }


    public boolean saveUserInfo(UserInfo userInfo){
        boolean isSavedSuccessfully = false;
        if(!userInfo.getUserPasswd().isEmpty()){
            isSavedSuccessfully = saveUserInfo(userInfo, userInfo.getUserPasswd());
        } else {
            isSavedSuccessfully = saveUserInfo(userInfo, "");
        }
        return isSavedSuccessfully;
    }

    public boolean saveUserInfo(UserInfo userInfo, String password){
        boolean successfullySaved = false;
        int keyId = isUserExisted(userInfo);
        boolean isExisted = keyId >= 0 ? true: false;
        String encryptedPassword = "";
        try {
            encryptedPassword = AESCrypt.encrypt(password.trim());
        } catch (Exception e){
            e.printStackTrace();
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_COLUMN_ID, userInfo.getUserId());
        contentValues.put(TABLE_COLUMN_PHONE_NAME, userInfo.getUserPhoneName());
        contentValues.put(TABLE_COLUMN_ALIAS_NAME, userInfo.getUserAliasName());
        contentValues.put(TABLE_COLUMN_MAIL, userInfo.getUserEmail());
        contentValues.put(TABLE_COLUMN_PWD, encryptedPassword);
        contentValues.put(TABLE_COLUMN_REGISTE_TIME, userInfo.getUserRegisteTime());
        contentValues.put(TABLE_COLUMN_LOGIN_STATUS, userInfo.getUserLoginStatus());
        contentValues.put(TABLE_COLUMN_COMMUNITY_ID, userInfo.getUserCommunityId());
        contentValues.put(TABLE_COLUMN_REGION, userInfo.getUserRegion());
        contentValues.put(TABLE_COLUMN_PROVINCE, userInfo.getProvince());
        contentValues.put(TABLE_COLUMN_CITY, userInfo.getCity());
        contentValues.put(TABLE_COLUMN_AREA, userInfo.getArea());
        contentValues.put(TABLE_COLUMN_ADDR, userInfo.getAddress());
        contentValues.put(TABLE_COLUMN_LOGIN_STATUS, userInfo.getIsLastLogin()?1:0);


        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }
        if(isExisted){
            String where = TABLE_COLUMN_ID + " = " + keyId;
            if (mWritableDatabase.update(USERS_TABLE, contentValues, where, null) > 0) {
                successfullySaved = true;
            };
        } else {
            if (mWritableDatabase.insert(USERS_TABLE, null, contentValues) > 0) {
                successfullySaved = true;
            }
        }
        mContext.getContentResolver().notifyChange(USERS_TABLE_URI, null);
        return successfullySaved;
    }

    public void cleanUser(){
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }
        mWritableDatabase.execSQL("delete from "+USERS_TABLE);
    }

    public void disableAllUserActived(){
                /*disable all user as de-actived*/
        final String mSQL = "update "+USERS_TABLE+" set "+ TABLE_COLUMN_LOGIN_STATUS+" = 0 where "+TABLE_COLUMN_LOGIN_STATUS + " = 1;";
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }
        mWritableDatabase.execSQL(mSQL);

        final String mSQL2 = "update "+USERS_TABLE+" set "+ TABLE_COLUMN_LASTLOGIN+" = 0 where "+TABLE_COLUMN_LASTLOGIN + " = 1;";
        mWritableDatabase.execSQL(mSQL2);
    }

    public void updateActiveUser(UserInfo activeUserInfo){
        if(activeUserInfo.getUserLoginStatus() != 1) {
            Log.e(TAG, "This user is not actived");
            return;
        }

        //disableAllUserActived();
        cleanUser();
        //save or update actived user
        if (!activeUserInfo.getUserPasswd().isEmpty()) {
            saveUserInfo(activeUserInfo, activeUserInfo.getUserPasswd());
        } else {
            saveUserInfo(activeUserInfo, "");
        }
    }

    private UserInfo getUserInfoFromCursor(Cursor cursor){
        if(cursor != null && cursor.getCount() >= 1){
            cursor.moveToFirst();
            UserInfo userInfo = new UserInfo();
            userInfo.setUserPhoneName(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_PHONE_NAME)));
            userInfo.setUserId(Integer.parseInt(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_ID))));

            String aliasName = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_ALIAS_NAME));
            userInfo.setUserAliasName(aliasName);

            String email = cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_MAIL));
            userInfo.setUserEmail(email);

            String paasswd =  cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_PWD));
            try {
                userInfo.setUserPasswd(AESCrypt.decrypt(paasswd));
            }catch (Exception e){
                e.printStackTrace();
            }

            int registTime = cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_REGISTE_TIME));
            userInfo.setUserRegisteTime(registTime);
            userInfo.setUserLoginStatus(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_LOGIN_STATUS)));
            userInfo.setUserCommunityId(cursor.getInt(cursor.getColumnIndex(TABLE_COLUMN_COMMUNITY_ID)));
            userInfo.setUserRegion(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_REGION)));
            userInfo.setProvince(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_PROVINCE)));
            userInfo.setCity(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_CITY)));
            userInfo.setArea(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_AREA)));
            userInfo.setAddress(cursor.getString(cursor.getColumnIndex(TABLE_COLUMN_ADDR)));
            return userInfo;
        }
        return null;

    }

    public UserInfo getUserInfo(String username){
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        Cursor cursor = mReadableDatabase.query(true, USERS_TABLE, null,
                TABLE_COLUMN_PHONE_NAME + "=?", new String[]{username}, null, null, null, null);

        return getUserInfoFromCursor(cursor);
    }

    public UserInfo getActiveUserInfo(){
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }

        Cursor cursor = mReadableDatabase.query(true, USERS_TABLE, null,
                TABLE_COLUMN_LOGIN_STATUS +"=?", new String[]{"1"}, null, null, null, null);
        UserInfo info = getUserInfoFromCursor(cursor);
        cursor.close();
        return info;
    }

    private void setLastLoginUser(UserInfo info){
        final String SQL = "update "+USERS_TABLE+" set "+ TABLE_COLUMN_LASTLOGIN+" = 0 where "+TABLE_COLUMN_LASTLOGIN + " = 1;";
        Log.d(TAG, "setLastLoginUser, SQL = " + SQL);
        if(mWritableDatabase == null){
            mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        }
        mWritableDatabase.execSQL(SQL);

        String sql =  "update "+USERS_TABLE+" set "+ TABLE_COLUMN_LASTLOGIN+" = 1 where "+TABLE_COLUMN_PHONE_NAME +
                " = "+info.getUserPhoneName()+";";
        Log.d(TAG, "setLastLoginUser, sql = " + sql);
        mWritableDatabase.execSQL(sql);
    }

    public UserInfo getLastLoginUserInfo(){
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }
        Cursor cursor = mReadableDatabase.query(true, USERS_TABLE, null,
                TABLE_COLUMN_LASTLOGIN +"=?", new String[]{"1"}, null, null, null, null);
        UserInfo info = getUserInfoFromCursor(cursor);
        cursor.close();
        return info;
    }

    public String getUserPassword(UserInfo userInfo){
        int keyId = isUserExisted(userInfo);
        if(keyId < 0) return "";
        String encryptedPassword = "";
        if(mReadableDatabase == null){
            mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        }
        Cursor cursor = mReadableDatabase.query(true, USERS_TABLE, new String[]{TABLE_COLUMN_PWD},
                TABLE_COLUMN_PHONE_NAME +"=?", new String[]{userInfo.getUserPhoneName()}, null, null, null, null);
        if (cursor.getCount() == 1){
            cursor.moveToFirst();
            encryptedPassword = cursor.getString(0);
        }
        cursor.close();
        String password = "";
        try {
            password = AESCrypt.decrypt(encryptedPassword.trim());
        } catch (Exception e){
            e.printStackTrace();
        }
        return password;
    }

    public void saveUserPassword(UserInfo userInfo, String password){
        int keyId = isUserExisted(userInfo);
        saveUserInfo(userInfo, password);
    }

    public boolean registe(String username, String userpwd) {
        boolean registSuccessfully = false;
        int status_code=-1;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_ENDUSERNAME, username);
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_ENDUSERPWD, userpwd);

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_REGISTE, parametersMap);
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
                if(status_code != ErrorCodeMap.ERRNO_REG_SUCCESSFULLY){
                    ExceptionCenter.process(mContext, new ErrorCode(status_code, message));
                } else {
                    registSuccessfully = true;
                }
            } catch (JSONException e) {
                ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_PARSER_RESULT_JSON));
            }
        } else {
            ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_GET_RESULT_JSON));
        }
        if (status_code == ErrorCodeMap.ERRNO_REG_SUCCESSFULLY) {
            UserInfo userInfo = new UserInfo();
            userInfo.setUserPhoneName(username);
            userInfo.setUserPasswd(userpwd);
            registSuccessfully = saveUserInfo(userInfo);
        }

        return registSuccessfully;
    }

    public boolean addUserExternInfo(String username, String email, String province,
                                     String city, String area, String address) throws RemoteException {
        boolean isSuccessful = false;
        int status_code = -1;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_ENDUSERNAME, username);
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_EMAIL, email);
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_PROVINCE, province);
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_CITY, city);
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_AREA, area);
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_ADDR, address);

        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_REG_EXTERN_INFO, parametersMap);
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
                if(status_code != ErrorCodeMap.ERRNO_ADD_USER_INFO_SUCCESSFULLY){
                    ExceptionCenter.process(mContext, new ErrorCode(status_code, message));
                } else {
                    isSuccessful = true;
                }
            } catch (JSONException e) {
                ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_PARSER_RESULT_JSON));
            }
        } else {
            ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_GET_RESULT_JSON));
        }
        if(isSuccessful){
            UserInfo info = getUserInfo(username);
            if(info!=null) {
                info.setUserEmail(email);
                info.setProvince(province);
                info.setCity(city);
                info.setArea(area);
                info.setAddress(address);
                saveUserInfo(info);
            }else{
                ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_REG_USER_CAN_NOT_FIND));
            }
        }

        return isSuccessful;

    }

    private Map<String, String> getParameters(UserInfo info) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(TABLE_COLUMN_ID, info.getUserId()+"");
        parameters.put(TABLE_COLUMN_PHONE_NAME, info.getUserPhoneName());
        parameters.put(TABLE_COLUMN_ALIAS_NAME, info.getUserAliasName());
        parameters.put(TABLE_COLUMN_MAIL, info.getUserEmail());
        try {
            parameters.put(TABLE_COLUMN_PWD, AESCrypt.decrypt(info.getUserPasswd()));
        } catch (Exception e){
            e.printStackTrace();
        }
        parameters.put(TABLE_COLUMN_REGISTE_TIME, info.getUserRegisteTime()+"");
        parameters.put(TABLE_COLUMN_LOGIN_STATUS, info.getUserLoginStatus()+"");
        parameters.put(TABLE_COLUMN_COMMUNITY_ID, info.getUserCommunityId()+"");
        parameters.put(TABLE_COLUMN_REGION, info.getUserRegion());
        parameters.put(TABLE_COLUMN_PROVINCE, info.getProvince());
        parameters.put(TABLE_COLUMN_CITY, info.getCity());
        parameters.put(TABLE_COLUMN_AREA, info.getArea());
        parameters.put(TABLE_COLUMN_ADDR, info.getAddress());

        return parameters;
    }

    public int updateUserInfo(UserInfo info) {
        int updateRetCode = -1;
        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_UPDATE, getParameters(info));
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
                if (resultString.trim().equals(Constant.ErrorCode.UPDATE_FAILED_MESSAGE))
                    updateRetCode = Constant.ErrorCode.UPDATE_FAILED_CODE;

                if (resultString.trim().equals(Constant.ErrorCode.UPDATE_SUCCESSFULLY_MESSAGE))
                    updateRetCode = Constant.ErrorCode.UPDATE_SUCCESSFULLY_CODE;

                if (resultString.trim().equals(Constant.ErrorCode.UPDATE_FAILED_KEY_ERROR_MESSAGE))
                    updateRetCode = Constant.ErrorCode.UPDATE_FAILED_KEY_ERROR_CODE;
            } catch (JSONException e) {
                updateRetCode = Constant.ErrorCode.UPDATE_FAILED_CODE;
                e.printStackTrace();
            }
        } else {
            updateRetCode = Constant.ErrorCode.UPDATE_FAILED_CODE;
        }
        if (updateRetCode == Constant.ErrorCode.UPDATE_SUCCESSFULLY_CODE) {
            info.setUserLoginStatus(1);

            Calendar c = Calendar.getInstance();
            String time = c.get(Calendar.YEAR) + "-" +
                    (c.get(Calendar.MONTH) + 1) + "-" +
                    (c.get(Calendar.DAY_OF_MONTH)) + " " +
                    (c.get(Calendar.HOUR_OF_DAY)) + ":" +
                    (c.get(Calendar.MINUTE)) + ":" +
                    (c.get(Calendar.SECOND));
//            info.setUserLastLoginTime(time);
            mDatabaseHelper.saveUserInfo(info);
        }
        return updateRetCode;
    }

    public int changePassword(String oldPassword, String keyCode, String newPassword) throws RemoteException {
        Log.d(TAG, "changePassword");
        int changePwdRetCode = -1;
        Map<String, String> parameters = new HashMap<>();
        parameters.put(USERNAME_FIELD, mCurrentUser.getUserPhoneName());
        parameters.put(USERID_FIELD, "" + mCurrentUser.getUserId());
        parameters.put(PASSWD_FIELD, oldPassword);
        parameters.put(KEY_CODE_FIELD, keyCode);
        parameters.put(NEWPASSWD_FIELD, newPassword);
        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_CHANGEPASSWD, parameters);
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
                if (resultString.trim().equals(Constant.ErrorCode.CHANGEPASSWD_FAILED_MESSAGE))
                    changePwdRetCode = Constant.ErrorCode.CHANGEPASSWD_FAILED_CODE;

                if (resultString.trim().equals(Constant.ErrorCode.CHANGEPASSWD_FAILED_PARAMETER_ERROR_MESSAGE))
                    changePwdRetCode = Constant.ErrorCode.CHANGEPASSWD_FAILED_PARAMETER_ERROR_CODE;

                if (resultString.trim().equals(Constant.ErrorCode.CHANGEPASSWD_SUCCESSFULLY_MESSAGE))
                    changePwdRetCode = Constant.ErrorCode.CHANGEPASSWD_SUCCESSFULLY_CODE;
            } catch (JSONException e) {
                changePwdRetCode = Constant.ErrorCode.CHANGEPASSWD_FAILED_CODE;
                e.printStackTrace();
            }
        } else {
            changePwdRetCode = Constant.ErrorCode.CHANGEPASSWD_FAILED_CODE;
        }

        if (changePwdRetCode == Constant.ErrorCode.CHANGEPASSWD_SUCCESSFULLY_CODE) {
            UserInfo userInfo = mCurrentUser;
            userInfo.setUserPasswd(parameters.get("userpwdnew"));
            userInfo.setUserLoginStatus(0);

            Calendar c = Calendar.getInstance();
            String time = c.get(Calendar.YEAR) + "-" +
                    (c.get(Calendar.MONTH) + 1) + "-" +
                    (c.get(Calendar.DAY_OF_MONTH)) + " " +
                    (c.get(Calendar.HOUR_OF_DAY)) + ":" +
                    (c.get(Calendar.MINUTE)) + ":" +
                    (c.get(Calendar.SECOND));
//            userInfo.setUserLastLoginTime(time);
            mDatabaseHelper.saveUserInfo(userInfo);
        }
        return changePwdRetCode;
    }

    public boolean login(String username, String password) throws RemoteException {
        boolean isSuccessfully = false;
        Map<String, String> parametersMap = new HashMap<>();
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_ENDUSERNAME, username);
        parametersMap.put(ELookServiceImpl.HTTP_PARAMS_ENDUSERPWD, password);
        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_LOGIN, parametersMap);
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
                String message = resultObject.getString("ret_message");
                if(status_code != ErrorCodeMap.ERRNO_LOGIN_SUCCESSFULLY){
                    ExceptionCenter.process(mContext, new ErrorCode(status_code, message));
                } else {
                    isSuccessfully = true;
                }
            } catch (JSONException e) {
                ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_PARSER_RESULT_JSON));
            }
        } else {
            ExceptionCenter.process(mContext, new ErrorCode(ErrorCodeMap.ERROR_CANNOT_GET_RESULT_JSON));
        }
        if(isSuccessfully){
            try {
                JSONObject jsonObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                JSONObject userInfoDataJson = jsonObject.getJSONObject("data");
                UserInfo userInfo = new UserInfo();
                userInfo.parserUserInfoJson(userInfoDataJson);
                userInfo.setLastLogin(true);
                updateActiveUser(userInfo);
                setLastLoginUser(userInfo);

                cleanAdverInfos();
                JSONArray  advertArray = jsonObject.getJSONArray("advertising");
                for(int i=0;i<advertArray.length();i++){
                    JSONObject advert = advertArray.getJSONObject(i);
                    AdvertInfo advertInfo = new AdvertInfo();
                    advertInfo.parserUserInfoJson(advert);
                    mDatabaseHelper.saveAdvertInfo(advertInfo);
                }

            } catch (JSONException e){
                e.printStackTrace();
            }
        }

        return isSuccessfully;
    }

    public int changPasswdWithPhoneMsg(String phonenumber, String newPasswd, String confirmNewPasswd) throws RemoteException {
        int ret = -1;
        StringBuffer sb = new StringBuffer();
        sb.append("endusername="+phonenumber+"&");
        sb.append("newpwd="+newPasswd+"&");
        String parameters =sb.toString();
        LoadDataFromServerTask task = new LoadDataFromServerTask(Constant.URL_FETCH_FIND_PWD, parameters);
        Future<JSONObject> connectionResult = mThreadPool.submit(task);
        JSONObject data = null;
        try {
            data = connectionResult.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(data != null) {
            JSONObject resultObject = null;
            try {
                resultObject = data.getJSONObject("UserInfo").getJSONObject("ret");
                int status_code = resultObject.getInt("status_code");
                if(status_code == ErrorCodeMap.ERRNO_FIND_PWD_SUCCESSFULLY){
                    ret = 1 ;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }
}
