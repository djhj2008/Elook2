package com.elook.client.user;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import com.elook.client.utils.Constant;
import com.elook.client.utils.ELUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by haiming on 3/23/16.
 */
/*
{    "access_autoid":"6",
    "access_device_id":"123456789",
    "access_value":"321",
    "access_time":"1464332775",
    "access_new_url":null
}
*/
public class MeasurementRecord implements Parcelable{

    private int mRecordId;
    private int mDateTime = 1451606400;//default time in seconds 2016-01-01
    private int mDevId;
    private int mValue = 0;
    private String mNewPicUrl;

    public static final Parcelable.Creator<MeasurementRecord> CREATOR = new ClassLoaderCreator<MeasurementRecord>() {
        @Override
        public MeasurementRecord createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }

        @Override
        public MeasurementRecord createFromParcel(Parcel source) {
            return new MeasurementRecord(source);
        }

        @Override
        public MeasurementRecord[] newArray(int size) {
            return new MeasurementRecord[size];
        }
    };

    public  MeasurementRecord(){}

    public MeasurementRecord(Parcel source){
        readFromParcel(source);
    }

    public MeasurementRecord(JSONObject dataJson){
        String devid = "";
        int datetime = 0;
        int value = 0;
        if(dataJson != null){
            try {
                String timeStr = dataJson.getString("access_time");
                if(timeStr != null && !timeStr.isEmpty() && !timeStr.equals("null")){
                    datetime = Integer.parseInt(timeStr);
                } else {
                    datetime = (int)SystemClock.elapsedRealtime();
                }
                String tmpStr = dataJson.getString("access_autoid");
                if(isAvaliableInteger(tmpStr)) this.mRecordId = Integer.parseInt(tmpStr);


                tmpStr = dataJson.getString("access_device_id");
                if(isAvaliableInteger(tmpStr)) this.mDevId = Integer.parseInt(tmpStr);

                tmpStr = dataJson.getString("access_value");
                if(isAvaliableInteger(tmpStr)) this.mValue = Integer.parseInt(tmpStr);
                this.mNewPicUrl = dataJson.getString("access_new_url");
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        this.mDateTime = datetime;
        Log.d("MeasurementRecord", "autoid="+mRecordId+"  deviceid="+mDevId+" accessvalue="+mValue+" mDateTime="+mDateTime);
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

    public MeasurementRecord(int recordId, int devId, int datetime, int values){
        this.mRecordId = recordId;
        this.mDateTime = datetime;
        this.mValue = values;
        this.mDevId = devId;
    }

    public int getRecordId() {
        return mRecordId;
    }

    public void setRecordId(int recordId) {
        this.mRecordId = recordId;
    }

    public void setValues(int values) {
        this.mValue = values;
    }

    public void setDateTime(int dateTime) {
        this.mDateTime = dateTime;
    }

    public int getRecordValue(){
        return mValue;
    }

    public int getDateTime(){
        return mDateTime;
    }

    public int getDevId() {
        return mDevId;
    }

    public void setDevId(int devId) {
        this.mDevId = devId;
    }

    public String getNewPicUrl() {
        return mNewPicUrl;
    }

    public void setNewPicUrl(String newPicUrl) {
        this.mNewPicUrl = newPicUrl;
    }

    public void readFromParcel(Parcel source){
        this.mRecordId = source.readInt();
        this.mDateTime = source.readInt();
        this.mDevId = source.readInt();
        this.mValue = source.readInt();
        this.mNewPicUrl = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRecordId);
        dest.writeInt(this.mDateTime);
        dest.writeInt(this.mDevId);
        dest.writeInt(this.mValue);
        dest.writeString(this.mNewPicUrl);
    }
}
