package com.elook.client.user;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;

import org.json.JSONException;
import org.json.JSONObject;

/*day week month year*/
/*{"day":1466784000,"value":null}*/

public class MeasurementCountData implements Parcelable{

    private int mRecordId;
    private int mDateTime;//default time in seconds 2016-01-01
    private int mDevId;
    private int mValue = 0;
    private int mType;

    public static final Creator<MeasurementCountData> CREATOR = new ClassLoaderCreator<MeasurementCountData>() {
        @Override
        public MeasurementCountData createFromParcel(Parcel source, ClassLoader loader) {
            return null;
        }

        @Override
        public MeasurementCountData createFromParcel(Parcel source) {
            return new MeasurementCountData(source);
        }

        @Override
        public MeasurementCountData[] newArray(int size) {
            return new MeasurementCountData[size];
        }
    };

    public MeasurementCountData(){}

    public MeasurementCountData(Parcel source){
        readFromParcel(source);
    }

    public MeasurementCountData(JSONObject dataJson ,String devi_id , int type){
        int datetime = 0;
        int value = 0;
        if(dataJson != null){
            try {
                String timeStr = dataJson.getString("day");
                if(timeStr != null && !timeStr.isEmpty() && !timeStr.equals("null")){
                    datetime = Integer.parseInt(timeStr);
                } else {
                    datetime = (int)SystemClock.elapsedRealtime();
                }

                String tmpStr = dataJson.getString("value");
                if(isAvaliableInteger(tmpStr)) {
                    this.mValue = Integer.parseInt(tmpStr);
                }else{
                    this.mValue = -1;
                }

                this.mType = type;
                if(isAvaliableInteger(devi_id)) {
                    this.mDevId = Integer.parseInt(devi_id);
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        this.mDateTime = datetime;


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

    public MeasurementCountData(int recordId, int devId, int datetime, int values ,int type){
        this.mRecordId = recordId;
        this.mDateTime = datetime;
        this.mValue = values;
        this.mDevId = devId;
        this.mType = type;
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

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public void readFromParcel(Parcel source){
        this.mRecordId = source.readInt();
        this.mDateTime = source.readInt();
        this.mDevId = source.readInt();
        this.mValue = source.readInt();
        this.mType = source.readInt();
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
        dest.writeInt(this.mType);
    }
}
