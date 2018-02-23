package com.elook.client.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.elook.client.user.AccessPointInfo;
import com.elook.client.utils.AESCrypt;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by haiming on 5/16/16.
 */
public class AccessPointTable {
    public static final String CONFIGED_AP_TABLE = "access_point";
    public static final Uri CONFIGED_AP_TABLE_URI = Uri.parse(ELookDatabaseHelper.BASE_URI+CONFIGED_AP_TABLE);
    /*Access Point and Device info*/
    private static final String KEY_ID = "_id";
    private static final String NAME_COLUMN = "name";
    private static final String MAC_COLUMN = "mac";
    private static final String SECUR_COLUME = "security_type";
    private static final String ACTIVIED_COLUME = "is_actived";
    private static final String PASSWORD = "key";

    public static final String CREATE_AP_TABLE_SQL = "CREATE TABLE if not exists "+ CONFIGED_AP_TABLE + " ("+
            KEY_ID +" integer PRIMARY KEY autoincrement, "+
            NAME_COLUMN + " text, " +
            MAC_COLUMN + " text not null, "+
            SECUR_COLUME + " text not null, "+
            ACTIVIED_COLUME + " integer not null default 0, "+
            PASSWORD + " text not null);";

    private ELookDatabaseHelper mDatabaseHelper;
    private SQLiteDatabase mReadableDatabase, mWritableDatabase;
    private Context mContext;
    public AccessPointTable(Context c, ELookDatabaseHelper dbHelper){
        if(dbHelper != null){
            this.mDatabaseHelper = dbHelper;
            mReadableDatabase = dbHelper.getReadableDatabase();
            mWritableDatabase = dbHelper.getWritableDatabase();
        }
        this.mContext = c;

    }


    private String revertMac(String dirtyMac){
        char[] mac = dirtyMac.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mac.length; i++){
            sb.append(mac[i]);
            if( (i<mac.length-1) && (i+1)%2 == 0)sb.append(":");
        }
        return sb.toString();
    }

    public void disableAllAPActived(){
        if(mWritableDatabase == null)mWritableDatabase = mDatabaseHelper.getWritableDatabase();
                /*disable all user as de-actived*/
        final String SQL = "update "+CONFIGED_AP_TABLE +" set is_actived = 0 where is_actived = 1;";
        mWritableDatabase.execSQL(SQL);
    }

    public int isApInfoExisted(AccessPointInfo apInfo){
        if(mReadableDatabase == null)mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = mReadableDatabase.query(true, CONFIGED_AP_TABLE, new String[]{KEY_ID, MAC_COLUMN},
                null, null, null, null, null, null);
        int keyId = -1;
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            String cleanApMac = apInfo.getmacAdrress();//.replace(":", "");
            Log.d("doudou","isApInfoExisted cleanApMac:"+cleanApMac);
            for (int i = 0; i < cursor.getCount(); i++){
                String cleanSavedMac = cursor.getString(cursor.getColumnIndex(MAC_COLUMN));
                Log.d("doudou","isApInfoExisted cleanSavedMac:"+cleanSavedMac);
                if (cleanApMac.equalsIgnoreCase(cleanSavedMac)){
                    keyId = cursor.getInt(cursor.getColumnIndex(KEY_ID));
                    break;
                }
                cursor.moveToNext();
            }
        }
        cursor.close();

        return keyId;
    }
    public boolean saveApPasswd(AccessPointInfo apInfo, String password){
        disableAllAPActived();
        int keyId = isApInfoExisted(apInfo);
        Log.d("doudou","saveApPasswd keyId:"+keyId);
        boolean isExisted = keyId > 0 ? true: false;
        boolean isSaved = false;
        String encryptedPassword = "";
        try {
            encryptedPassword = AESCrypt.encrypt(password.trim());
        } catch (Exception e){
            e.printStackTrace();
        }
//        String cleanApMac = apInfo.getmacAdrress().replace(":", "");
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME_COLUMN, apInfo.getName());
        contentValues.put(MAC_COLUMN, apInfo.getmacAdrress());
        contentValues.put(SECUR_COLUME, apInfo.getsecurityType());
        contentValues.put(ACTIVIED_COLUME, 1);
        contentValues.put(PASSWORD, encryptedPassword);
        if(mWritableDatabase == null)mWritableDatabase = mDatabaseHelper.getWritableDatabase();
        if(isExisted){
            String where = KEY_ID + " = " + keyId;
            if (mWritableDatabase.update(CONFIGED_AP_TABLE, contentValues, where, null) > 0) {
                isSaved = true;
            }
        } else {
            if (mWritableDatabase.insert(CONFIGED_AP_TABLE, null, contentValues) > 0) {
                isSaved = true;
            }
        }
        return isSaved;
    }

    public AccessPointInfo getActivedApInfo(){
        AccessPointInfo activedApInfo =  new AccessPointInfo();
        if(mReadableDatabase == null)mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        String rawSQL = "select * from " + CONFIGED_AP_TABLE + " where "+ACTIVIED_COLUME + " = 1;";
        Cursor cursor = mReadableDatabase.rawQuery(rawSQL, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                activedApInfo.setName(cursor.getString(cursor.getColumnIndex(NAME_COLUMN)));
                activedApInfo.setmacAdrress(cursor.getString(cursor.getColumnIndex(MAC_COLUMN)));
                activedApInfo.setsecurityType(Integer.parseInt(cursor.getString(cursor.getColumnIndex(SECUR_COLUME))));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  activedApInfo;
    }

    public List<AccessPointInfo> getConnectedAps(){
        List<AccessPointInfo> hasConnectedApInfos = new ArrayList<>();
        if(mReadableDatabase == null)mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = mReadableDatabase.query(true, CONFIGED_AP_TABLE, new String[]{
                        NAME_COLUMN, MAC_COLUMN, SECUR_COLUME, PASSWORD},
                null, null, null, null, null, null);

        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            do{
                AccessPointInfo apInfo = new AccessPointInfo();
                apInfo.setName(cursor.getString(0));
                apInfo.setmacAdrress(cursor.getString(1));
                apInfo.setsecurityType(Integer.parseInt(cursor.getString(2).trim()));
                hasConnectedApInfos.add(apInfo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  hasConnectedApInfos;
    }

    public String getPasswordOfAp(AccessPointInfo apInfo){
        int keyId = isApInfoExisted(apInfo);
        if(keyId < 0) return "";
        String encryptedPassword = "";
        String cleanApMac = apInfo.getmacAdrress();//.replace(":", "");
        if(mReadableDatabase == null)mReadableDatabase = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = mReadableDatabase.query(true, CONFIGED_AP_TABLE, new String[]{PASSWORD},
                "mac=?", new String[]{cleanApMac}, null, null, null, null);
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            encryptedPassword = cursor.getString(cursor.getColumnIndex(PASSWORD));
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
}
